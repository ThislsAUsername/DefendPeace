package Units;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import CommandingOfficers.Commander;
import Engine.FloodFillFunctor;
import Engine.GameInstance;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.FloodFillFunctor.BasicMoveFillFunctor;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.CreateUnitEvent.AnimationStyle;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.KaijuActions.BirdSwoopFactory;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;
import Units.MoveTypes.MoveType;

public class KaijuWarsKaiju
{
  /*
   * Kaiju abilities I do not plan to implement at this time

   * Alpha
   *  - Tail Whip (Kill any adjacent unit that isn't on the PATH; not implementing PATH mechanics)
   * Hell Turkey
   *  - Fire trail (Place a crisis on any tile you move over; not implementing PATH mechanics)
   * Duggemundr
   *  - Heat Beam (Kill a unit at 1-2 range and plop down crises in radius 1; not implementing crises)
   * Big Donk
   *  - Wily (-1 damage taken while on forest, +1 move while on forest, move into adjacent forest on building kill; too much jank)
   * Flying Hubcap
   *  - Hologram (After movement, spawn a hologram within 3 tiles, and swap with it half the time; too much jank)
   */

  private static final int KAIJU_COST = 0;
  private static final int MOVE_POWER = 2; // Dummy value
  private static final double STAR_VALUE = 10.0;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MAX_AMMO = -1;
  private static final int VISION_RANGE = 2;

  private static final UnitActionFactory[] KAIJU_ACTIONS = { new KaijuActions.KaijuCrushFactory(), UnitActionFactory.DELETE };

  private static final MoveType KAIJU_MOVE = new FootKaiju();

  public static class KaijuUnitModel extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    public final int[] hpChunks, hpBases;

    public KaijuUnitModel(String pName, long pRole, int[] pHPchunks, int[] pHPbases)
    {
      super(pName, pRole, KAIJU_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, KAIJU_MOVE, KAIJU_ACTIONS, new WeaponModel[0], STAR_VALUE);

      resistsKaiju = true;
      isKaiju      = true;
      hpChunks     = pHPchunks;
      hpBases      = pHPbases;
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      KaijuUnitModel newModel = new KaijuUnitModel(name, role, hpChunks, hpBases);

      newModel.copyValues(this);
      return newModel;
    }
    public void copyValues(KaijuUnitModel other)
    {
      super.copyValues(other);
    }
  }

  public static final int BIRD_LAND_HP = 10; // Should match the first chunk below
  // These are the chunks of HP each Kaiju has in each tier of movement (they get slower as they take damage)
  // Index = move points
  public static final int[] ALPHA_CHUNKS = { 0, 0, 12, 18,  8,  7,  6 };
  public static final int[] BIRD_CHUNKS  = { 0, 0, 10, 10,  8,  4,  4 };
  public static final int[] DONK_CHUNKS  = { 0, 0, 10, 12,  8,  9 };
  public static final int[] SNEK_CHUNKS  = { 0, 0, 10, 15, 11 };
  // UFO starts at 3 move
  public static final int[] UFO_CHUNKS   = { 0, 0,  0,  9,  4,  4,  3,  3 };
  // From testing, it looks like everything beyond a certain point is just chunks of 8 for all Kaiju
  public static final int DEFAULT_HP_CHUNK = 8;

  // Kaiju start with one ability, get a second, then the third/fourth in tandem
  public enum KaijuAbilityTier { BASIC, EXTRA, ALL }
  public static final int EXTRA_SKILL_TURN = 5;
  public static final int ALL_SKILLS_TURN  = 20;
  // These are the "base" HP values for each kaiju at the skill breakpoint turns
  // Note that the turn number will be added to this value, in all cases
  // Also, these are made up/customizable in game (as are the breakpoint turns), so can be used as balancing factors
  public static final int[] ALPHA_HPBASES = { 12, 20, 38 };
  public static final int[] BIRD_HPBASES  = { 12, 20, 38 };
  public static final int[] DONK_HPBASES  = { 12, 12, 12 };
  public static final int[] SNEK_HPBASES  = { 12, 12, 12 };
  public static final int[] UFO_HPBASES   = { 12, 16, 12 };

  public static class Alphazaurus extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;
    public Alphazaurus()
    {
      super("Alphazaurus", ROLE, ALPHA_CHUNKS, ALPHA_HPBASES);
      addUnitModifier(new AlphazaurusMod());
    }

    /** Heal self by 3 on a cooldown */
    @Override
    public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
    {
      GameEventQueue events = super.getTurnInitEvents(self, map);

      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      if( kaijuTracker.isReady(self, Alphazaurus.class) )
      {
        // Setting the tracker state here feels wrong
        kaijuTracker.abilityUsedLong(self, Alphazaurus.class);
        events.add(new HealUnitEvent(self, 3, null, true));
      }
      return events;
    }
  }
  public static class AlphazaurusMod extends KaijuMoveMod
  {
    private static final long serialVersionUID = 1L;

    KaijuStateTracker kaijuTracker;
    @Override
    public void registerTrackers(GameInstance gi)
    {
      kaijuTracker = StateTracker.instance(gi, KaijuStateTracker.class);
    }

    @Override
    public void modifyActionList(UnitContext uc)
    {
      if(!kaijuTracker.kaijuAbilityTier.containsKey(uc.unit))
        return; // Not a registered kaiju

      final KaijuAbilityTier tier = kaijuTracker.kaijuAbilityTier.get(uc.unit);
      switch (tier)
      {
        case ALL:
          uc.possibleActions.add(new KaijuActions.AlphaBreathFactory());
          uc.possibleActions.add(new KaijuActions.AlphaKickFactory());
        case EXTRA:
          uc.possibleActions.add(new KaijuActions.AlphaTsunamiFactory());
        case BASIC:
          break;
      }
    }
  } //~AlphazaurusMod

  public static class HellTurkey extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = HOVER | AIR_HIGH;
    public HellTurkeyLand turkeyLand;
    public HellTurkeyEgg turkeyEgg;
    public HellTurkey()
    {
      super("Hell Turkey", ROLE, BIRD_CHUNKS, BIRD_HPBASES);
      addUnitModifier(new HellTurkeyMod());
    }
    // Transitions to the land turkey are handled by KaijuStateTracker and the Kaiju crush action
  }
  public static class HellTurkeyLand extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = HOVER | LAND;
    public final HellTurkey airTurkey;
    public HellTurkeyLand(HellTurkey turkey)
    {
      super("Hell Turkey Land", ROLE, BIRD_CHUNKS, BIRD_HPBASES);
      airTurkey = turkey;
      addUnitModifier(new HellTurkeyMod());
    }

    /** Heal self by 1 with Swoop, if able */
    @Override
    public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
    {
      GameEventQueue events = super.getTurnInitEvents(self, map);

      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      // If we have Swoop and are in heal mode
      if( kaijuTracker.kaijuAbilityTier.get(self) == KaijuAbilityTier.EXTRA
          && BIRD_LAND_HP >= self.getHP() )
      {
        // Setting the tracker state here feels wrong
        kaijuTracker.abilityUsedShort(self, BirdSwoopFactory.class);
        events.add(new HealUnitEvent(self, 1, null, true));
        // If we're about to go above the land HP, become flying
        if( self.getHP() == BIRD_LAND_HP )
          events.add(new TransformLifecycle.TransformEvent(self, airTurkey));
      }
      return events;
    }
  }
  public static class HellTurkeyEgg extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = LAND;
    public final HellTurkeyLand landTurkey;
    public HellTurkeyEgg(HellTurkeyLand turkey)
    {
      super("Hell Turkey Egg", ROLE, BIRD_CHUNKS, BIRD_HPBASES);
      landTurkey = turkey;
      this.baseMovePower = 0;
      // The egg may resurrect, and naught else
      this.baseActions.clear();
      this.baseActions.add(new KaijuActions.BirdResurrectFactory(landTurkey));
      // The egg is invincible
      this.addUnitModifier(new UnitDefenseModifier(300));
    }
  }
  public static class HellTurkeyMod extends KaijuMoveMod
  {
    private static final long serialVersionUID = 1L;

    KaijuStateTracker kaijuTracker;
    @Override
    public void registerTrackers(GameInstance gi)
    {
      kaijuTracker = StateTracker.instance(gi, KaijuStateTracker.class);
    }

    @Override
    public void modifyActionList(UnitContext uc)
    {
      if(!kaijuTracker.kaijuAbilityTier.containsKey(uc.unit))
        return; // Not a registered kaiju

      final KaijuAbilityTier tier = kaijuTracker.kaijuAbilityTier.get(uc.unit);
      switch (tier)
      {
        case ALL:
          // TODO: eruption/wind
        case EXTRA:
          uc.possibleActions.add(new KaijuActions.BirdSwoopFactory());
        case BASIC:
          break;
      }
    }
  } //~AlphazaurusMod

  /**
   * Handles cleanup after Kaiju are built:
   * <p>Gives them the proper HP count, and deletes the port
   * <p>Stores the creation turn to track abilities
   * <p>Also handles ability cooldowns
   */
  // Should building Kaiju be a different action than building other stuff?
  public static class KaijuStateTracker extends StateTracker
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameEventQueue receiveCreateUnitEvent(Unit unit)
    {
      if( !(unit.model instanceof KaijuUnitModel) )
        return null;

      final int turn = game.getCurrentTurn();

      KaijuAbilityTier tier = KaijuAbilityTier.BASIC;
      if( turn >= EXTRA_SKILL_TURN )
        tier = KaijuAbilityTier.EXTRA;
      if( turn >= ALL_SKILLS_TURN )
        tier = KaijuAbilityTier.ALL;
      kaijuAbilityTier.put(unit, tier);

      KaijuUnitModel kaijuType = (KaijuUnitModel) unit.model;
      int hp = kaijuType.hpBases[tier.ordinal()];
      hp += turn;
      int heal = hp - UnitModel.MAXIMUM_HP;

      GameEventQueue events = new GameEventQueue();
      events.add(new HealUnitEvent(unit, heal, null, true));

      XYCoord buildCoords = new XYCoord(unit);
      Environment env = game.gameMap.getEnvironment(buildCoords);
      if( null == env || env.terrainType != TerrainType.SEAPORT )
        return events;

      // Destroy the port
      Environment newEnvirons = Environment.getTile(env.terrainType.getBaseTerrain(), env.weatherType);
      events.add(new MapChangeEvent(buildCoords, newEnvirons));

      return events;
    }

    // Resurrect Hell Turkey as an egg
    private static final int FUDGE_RADIUS = 2;
    @Override
    public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
    {
      UnitModel egg;
      if( victim.model instanceof HellTurkey )
        egg = ((HellTurkey) victim.model).turkeyEgg;
      else if ( victim.model instanceof HellTurkeyLand)
        egg = ((HellTurkeyLand) victim.model).airTurkey.turkeyEgg;
      else // Not a resurrectable Kaiju
        return null;

      GameEventQueue events = new GameEventQueue();
      boolean unitIsReady = false;
      events.add(new CreateUnitEvent(victim.CO, egg, grave, AnimationStyle.DROP_IN, unitIsReady, FUDGE_RADIUS ));

      return events;
    }
    // Land Hell Turkey if it takes too much damage
    @Override
    public GameEventQueue receiveBattleEvent(BattleSummary summary)
    {
      final Unit victim = summary.defender.unit;
      if( !(victim.model instanceof KaijuUnitModel) )
        return null;
      if( summary.defender.after.getHP() > KaijuWarsKaiju.BIRD_LAND_HP )
        return null;

      GameEventQueue events = new GameEventQueue();
      tryDevolveHellTurkey(victim, events);
      return events;
    }
    // Land Hell Turkey if it takes too much damage
    @Override
    public GameEventQueue receiveMassDamageEvent(Commander attacker, Map<Unit, Integer> lostHP)
    {
      GameEventQueue events = new GameEventQueue();
      for( Entry<Unit, Integer> pair : lostHP.entrySet() )
      {
        final Unit victim = pair.getKey();
        if( !(victim.model instanceof KaijuUnitModel) )
          continue;
        if( pair.getValue() + KaijuWarsKaiju.BIRD_LAND_HP < victim.getHP() )
          continue;
        tryDevolveHellTurkey(victim, events);
      }
      return events;
    }
    public void tryDevolveHellTurkey(Unit actor, GameEventQueue events)
    {
      HellTurkeyLand devolveToType = null;
      if( actor.model instanceof HellTurkey )
        devolveToType = ((HellTurkey) actor.model).turkeyLand;
      if( devolveToType != null )
      {
        events.add(new TransformLifecycle.TransformEvent(actor, devolveToType));
      }
    }

    public HashMap<Unit, KaijuAbilityTier> kaijuAbilityTier = new HashMap<>();

    public HashMap<Unit, HashMap<Object, Integer>> kaijuAbilityTurns = new HashMap<>();
    public static final int KAIJU_ABILITY_COOLDOWN = 4;
    public void abilityUsedLong(Unit unit, Object key)
    {
      abilityUsed(unit, key, KAIJU_ABILITY_COOLDOWN);
    }
    public void abilityUsedShort(Unit unit, Object key)
    {
      abilityUsed(unit, key, 1);
    }
    public void abilityUsed(Unit unit, Object key, int duration)
    {
      if( !kaijuAbilityTurns.containsKey(unit) )
        kaijuAbilityTurns.put(unit, new HashMap<>());

      HashMap<Object, Integer> abilityUses = kaijuAbilityTurns.get(unit);
      final int readyTurn = game.getCurrentTurn() + duration;
      abilityUses.put(key, readyTurn);
    }
    public boolean isReady(Unit unit, Object key)
    {
      // Kaiju start with all abilities ready
      if( !kaijuAbilityTurns.containsKey(unit) )
        return true;
      HashMap<Object, Integer> abilityUses = kaijuAbilityTurns.get(unit);
      if( !abilityUses.containsKey(key) )
        return true;

      final Integer readyTurn = abilityUses.get(key);
      final int turn = game.getCurrentTurn();
      final boolean onCooldown = readyTurn > turn;

      return !onCooldown;
    }
  }
  
  // Movement/basic action stuffs

  // Sets Kaiju movement to the correct value based on HP
  public static class KaijuMoveMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void modifyMovePower(UnitContext uc)
    {
      if( !(uc.model instanceof KaijuUnitModel) )
        return;

      KaijuUnitModel kaijuType = (KaijuUnitModel) uc.model;
      int[] hpChunks = kaijuType.hpChunks;
      int hpBudget = uc.getHP();
      int move = 0;
      while (hpBudget > 0)
      {
        ++move;
        int chunk = DEFAULT_HP_CHUNK;
        if( hpChunks.length > move )
          chunk = hpChunks[move];
        hpBudget -= chunk;
      }
      uc.movePower = move;
    }
  } //~KaijuMoveMod

  public static class FootKaiju extends MoveType
  {
    private static final long serialVersionUID = 1L;

    public FootKaiju()
    {
      super();
      moveCosts.get(Weathers.CLEAR).setAllMovementCosts(1);
      moveCosts.get(Weathers.RAIN).setAllMovementCosts(1);
      moveCosts.get(Weathers.SNOW).setAllMovementCosts(1);
      moveCosts.get(Weathers.SANDSTORM).setAllMovementCosts(1);
    }
    public FootKaiju(FootKaiju other)
    {
      super(other);
    }

    @Override
    public FloodFillFunctor getUnitMoveFunctor(Unit mover, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      return new KaijuMoveFillFunctor(mover, this, includeOccupied, canTravelThroughEnemies);
    }

    @Override
    public MoveType clone()
    {
      return new FootKaiju(this);
    }
  }
  public static class KaijuMoveFillFunctor extends BasicMoveFillFunctor
  {
    public KaijuMoveFillFunctor(Unit mover, MoveType propulsion, boolean includeOccupied, boolean canTravelThroughEnemies)
    {
      super(mover, propulsion, includeOccupied, canTravelThroughEnemies);
    }

    @Override
    public int getTransitionCost(GameMap map, XYCoord from, XYCoord to)
    {
      // if we're past the edges of the map
      if( !map.isLocationValid(to) )
        return MoveType.IMPASSABLE;

      int cost = findMoveCost(from, to, map);

      // Cannot path through: Kaiju, buildings
      final MapLocation fromLocation = map.getLocation(from);
      if( !canTravelThroughEnemies
          && fromLocation.isCaptureable()
          && null != unit
          && unit.CO.isEnemy(fromLocation.getOwner()) )
        return MoveType.IMPASSABLE;

      if( null == unit )
        return cost;

      final Unit fromResident = fromLocation.getResident();
      if( null != fromResident && fromResident.CO.isEnemy(unit.CO) )
      {
        final KaijuWarsUnitModel fromResidentType = (KaijuWarsUnitModel) fromResident.model;
        if( !canTravelThroughEnemies && null != fromResidentType )
        {
          if( fromResidentType.isKaiju )
            cost = MoveType.IMPASSABLE;
        }
      }

      final Unit toResident = map.getLocation(to).getResident();
      if( null != toResident && toResident.CO.isEnemy(unit.CO) )
      {
        final KaijuWarsUnitModel toResidentType = (KaijuWarsUnitModel) toResident.model;
        if( !canTravelThroughEnemies && null != toResidentType )
        {
          // This results in slightly inaccurate behavior, but it's a slight nerf I can get behind.. and it's more consistent.
          // OG behavior was: If a slowing unit is at the edge of your move range, you can still crush the unit
          if( unit.model.isLandUnit()
              && toResidentType.slowsLand )
            cost += 1;
          if( unit.model.isAirUnit()
              && toResidentType.slowsAir )
            cost += 1;
        }
      }

      return cost;
    }

    @Override
    public boolean canEnd(GameMap map, XYCoord end)
    {
      return true;
    }
  } // ~KaijuMoveFillFunctor


}
