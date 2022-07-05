package Units;

import java.util.HashMap;
import Engine.FloodFillFunctor;
import Engine.GameInstance;
import Engine.StateTrackers.StateTracker;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.FloodFillFunctor.BasicMoveFillFunctor;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.MapChangeEvent;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
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

  // Kaiju start with one ability, get a second, then the third/fourth in tandem
  public enum KaijuAbilityTier { BASIC, EXTRA, ALL }

  private static final UnitActionFactory[] KAIJU_ACTIONS = { new KaijuActions.KaijuCrushFactory(), UnitActionFactory.DELETE };

  private static final MoveType KAIJU_MOVE = new FootKaiju();

  public static class KaijuUnitModel extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    // How do we do kaiju resurrection?
    // Kaiju get this + day number HP to start with
    public int turnZeroHP       = 12;
    // Kaiju start with one skill, and gain access to the others depending on their starting turn
    public int turnForSkillTwo  = 42;
    public int turnForAllSkills = 42;
    public final int[] hpChunks;

    public KaijuUnitModel(String pName, long pRole, int[] pHPchunks)
    {
      super(pName, pRole, KAIJU_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, KAIJU_MOVE, KAIJU_ACTIONS, new WeaponModel[0], STAR_VALUE);

      resistsKaiju = true;
      isKaiju      = true;
      hpChunks     = pHPchunks;
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      KaijuUnitModel newModel = new KaijuUnitModel(name, role, hpChunks);

      newModel.copyValues(this);
      return newModel;
    }
    public void copyValues(KaijuUnitModel other)
    {
      super.copyValues(other);
      turnZeroHP       = other.turnZeroHP;
      turnForSkillTwo  = other.turnForSkillTwo;
      turnForAllSkills = other.turnForAllSkills;
    }
  }

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

  public static class Alphazaurus extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | SURFACE_TO_AIR | TROOP | LAND;
    public Alphazaurus()
    {
      super("Alphazaurus", ROLE, ALPHA_CHUNKS);
      turnZeroHP       = 12;
      turnForSkillTwo  = 5;
      turnForAllSkills = 15;
      addUnitModifier(new AlphazaurusMod());
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
          // TODO
        case EXTRA:
          uc.possibleActions.add(new KaijuActions.AlphaTsunamiFactory());
        case BASIC:
          break;
      }
    }
  } //~KaijuMoveMod

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
      KaijuUnitModel kaijuType = (KaijuUnitModel) unit.model;
      int hp = kaijuType.turnZeroHP;
      hp += turn;
      int heal = hp - UnitModel.MAXIMUM_HP;

      GameEventQueue events = new GameEventQueue();
      events.add(new HealUnitEvent(unit, heal, null, true));

      KaijuAbilityTier tier = KaijuAbilityTier.BASIC;
      if( turn >= kaijuType.turnForSkillTwo )
        tier = KaijuAbilityTier.EXTRA;
      if( turn >= kaijuType.turnForAllSkills )
        tier = KaijuAbilityTier.ALL;
      kaijuAbilityTier.put(unit, tier);

      XYCoord buildCoords = new XYCoord(unit);
      Environment env = game.gameMap.getEnvironment(buildCoords);
      if( null == env || env.terrainType != TerrainType.SEAPORT )
        return events;

      // Destroy the port
      Environment newEnvirons = Environment.getTile(env.terrainType.getBaseTerrain(), env.weatherType);
      events.add(new MapChangeEvent(buildCoords, newEnvirons));

      return events;
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
        int chunk = DEFAULT_HP_CHUNK;
        if( hpChunks.length > move )
          chunk = hpChunks[move];
        ++move;
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
