package Units;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GamePath;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.CommanderEnergyChangeEvent;
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
import Units.KaijuActions.BirdResurrectFactory;
import Units.KaijuActions.BirdSwoopFactory;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;
import Units.KaijuWarsUnits.Radar;
import Units.MoveTypes.MoveType;

public class KaijuWarsKaiju
{
  /*
   * Kaiju abilities I do not plan to implement at this time

   * Alpha
   *  - Tail Swipe (Kill any adjacent unit that isn't on the PATH; not implementing PATH mechanics)
   * Hell Turkey
   *  - Molten Wings (Place a crisis on any tile you move over; not implementing crises)
   * Big Donk
   *  - Wily (-1 damage taken while on forest, +1 move while on forest, move into adjacent forest on building kill; too much jank)
   * Duggemundr
   *  - Heat Beam (Kill a unit at 1-2 range and plop down crises in radius 1 from the target; not implementing crises)
   * Flying Hubcap
   *  - Hologram (After movement, spawn a hologram within 3 tiles, and swap with it half the time; too much jank)
   */
  /*
   * Kaiju abilities I am implementing, in order of unlock; marking the last two with '+' since they're bundled

   * Alpha
   *  - Roar (Heal 3 HP)
   *  - Tsunami (Wreck an adjacent tile, if on water)
   *  + Kick (Kill an adjacent ground unit)
   *  + Breath (Wreck 1-2 range on an orthogonal)
   * Hell Turkey
   *  - Phoenix Burst (When you die, become an invincible egg. The egg then wrecks adjacent tiles and turns into a 5 HP bird)
   *  - Swoop (If grounded, heal 1 HP. Otherwise, kill an adjacent ground unit)
   *  + Wind Force (Kill an adjacent air unit)
   *  + Eruption (Kill all adjacent units; cooldown 4 turns)
   * Big Donk
   *  - Beat Chest (Heal 2 HP on killing a building)
   *  - Wind-up Punch (Target adjacent land unit, wreck 1-2 range)
   *  + Climb (Kill an adjacent air unit)
   *  + Rampage (Stomping a building no longer ends your movement, gain +2 speed every 4 turns)
   * Duggemundr
   *  - Ram (Kill the first unit in your path, or get +1 move)
   *  - Carapace (Take no damage from the first attack)
   *  + Burrow (Be invisible and untargetable if no Radar is within range 2; nerfing to "just" normal stealth)
   *  + Deep Tunnels (Take no counter or slows if no Radar is within range 2 of the victim)
   * Flying Hubcap
   *  - Beam Gun (Kill an adjacent unit, but lose 1 HP)
   *  - Nav Computer (+1 move if no radar is within range 2)
   *  + Abductions (Negate 1 security on stomping a building; implementing as +1 COP star)
   *  + EMP Pulse (Kill a Radar within range 2. Otherwise, kill all adjacent units)
   */

  private static final int KAIJU_COST = 0;
  private static final int MOVE_POWER = 2; // Dummy value
  private static final int STAR_VALUE = 50;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MAX_AMMO = -1;
  private static final int VISION_RANGE = 2;

  private static final UnitActionFactory[] KAIJU_ACTIONS = { new KaijuActions.KaijuCrushFactory(), UnitActionFactory.DELETE };

  private static final MoveType KAIJU_MOVE         = new FootKaiju(true);
  private static final MoveType KAIJU_MOVE_RAMPAGE = new FootKaiju(false);

  public static class KaijuUnitModel extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    public final int[] healthChunks, healthBases;
    public boolean regenOnBuildingKill  = false;
    public boolean chargeOnBuildingKill = false;
    public boolean hasRamSkill          = false;
    public boolean hasDeepTunnelSkill   = false;
    // Used by Big Donk and Duggemundr to get a new type on spawn
    public UnitModel promotesToAtAllSkills = null;

    public KaijuUnitModel(String pName, long pRole, int[] pHealthChunks, int[] pHealthBases)
    {
      super(pName, pRole, KAIJU_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, KAIJU_MOVE, KAIJU_ACTIONS, new WeaponModel[0], STAR_VALUE);

      resistsKaiju = true;
      isKaiju      = true;
      healthChunks = pHealthChunks;
      healthBases  = pHealthBases;
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      KaijuUnitModel newModel = new KaijuUnitModel(name, role, healthChunks, healthBases);

      newModel.copyValues(this);
      return newModel;
    }
    public void copyValues(KaijuUnitModel other)
    {
      super.copyValues(other);
      regenOnBuildingKill   = other.regenOnBuildingKill;
      chargeOnBuildingKill  = other.chargeOnBuildingKill;
      hasRamSkill           = other.hasRamSkill;
      promotesToAtAllSkills = other.promotesToAtAllSkills;
    }
  }

  public static final int BIRD_LAND_HEALTH = 100; // Should match the first chunk below
  // These are the chunks of health each Kaiju has in each tier of movement (they get slower as they take damage)
  // Index = move points
  public static final int[] ALPHA_CHUNKS = { 0, 0, 120, 180,  80,  70,  60 };
  public static final int[] BIRD_CHUNKS  = { 0, 0, 100, 100,  80,  40,  40 };
  public static final int[] DONK_CHUNKS  = { 0, 0, 100, 120,  80,  90 };
  public static final int[] SNEK_CHUNKS  = { 0, 0, 100, 150, 110 };
  // UFO starts at 3 move
  public static final int[] UFO_CHUNKS   = { 0, 0,  0,  90,  40,  40,  30,  30 };
  // From testing, it looks like everything beyond a certain point is just chunks of 8 HP for all Kaiju
  public static final int DEFAULT_HEALTH_CHUNK = 80;

  // Kaiju start with one ability, get a second, then the third/fourth in tandem
  public enum KaijuAbilityTier { BASIC, EXTRA, ALL }
  public static final int EXTRA_SKILL_TURN = 5;
  public static final int ALL_SKILLS_TURN  = 20;
  // These are the "base" health values for each kaiju at the skill breakpoint turns
  // Note that the turn number will be added to this value, in all cases
  // Also, these are made up/customizable in game (as are the breakpoint turns), so can be used as balancing factors
  public static final int[] ALPHA_HPBASES = { 50, 100, 130 };
  public static final int[] BIRD_HPBASES  = { 50, 100, 130 };
  public static final int[] DONK_HPBASES  = { 50,  50,   0 };
  public static final int[] SNEK_HPBASES  = { 50,  50,  50 };
  public static final int[] UFO_HPBASES   = { 50,  50,   0 };

  public static class Alphazaurus extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND | SEA;
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
        events.add(new HealUnitEvent(self, 30, null, true));
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
          uc.actionTypes.add(new KaijuActions.AlphaBreathFactory());
          uc.actionTypes.add(new KaijuActions.AlphaKickFactory());
        case EXTRA:
          uc.actionTypes.add(new KaijuActions.AlphaTsunamiFactory());
        case BASIC:
          break;
      }
    }
  } //~AlphazaurusMod

  public static class HellTurkey extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = HOVER | AIR_LOW;
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
      if( kaijuTracker.kaijuAbilityTier.get(self) != KaijuAbilityTier.BASIC
          && BIRD_LAND_HEALTH >= self.getHealth() )
      {
        // Setting the tracker state here feels wrong
        kaijuTracker.abilityUsedShort(self, BirdSwoopFactory.class);
        events.add(new HealUnitEvent(self, 10, null, true));
        // If we're about to go above the land HP, become flying
        // TODO: Since this is only handled here and there's no HealUnitEvent listener, this won't account for healing CO powers
        // ... nor, in fact, will healing CO powers actually heal the Kaiju over 10 HP by default
        if( self.getHealth() == BIRD_LAND_HEALTH )
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
          uc.actionTypes.add(new KaijuActions.BirdEruptionFactory());
          uc.actionTypes.add(new KaijuActions.BirdWindForceFactory());
        case EXTRA:
          uc.actionTypes.add(new KaijuActions.BirdSwoopFactory());
        case BASIC:
          break;
      }
    }
  } //~HellTurkeyMod

  public static class BigDonk extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;
    public BigDonk(BigDonkRampage rampager)
    {
      super("Big Donk", ROLE, DONK_CHUNKS, DONK_HPBASES);
      regenOnBuildingKill = true;
      promotesToAtAllSkills = rampager;
      addUnitModifier(new BigDonkMod());
    }
  }
  // Variant determined on spawn by KaijuStateTracker
  public static class BigDonkRampage extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;
    public BigDonkRampage()
    {
      super("Big Donk", ROLE, DONK_CHUNKS, DONK_HPBASES);
      regenOnBuildingKill = true;
      baseMoveType = KAIJU_MOVE_RAMPAGE;
      addUnitModifier(new BigDonkMod());
    }
  }
  public static class BigDonkMod extends KaijuMoveMod
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
          uc.actionTypes.add(new KaijuActions.DonkClimbFactory());
        case EXTRA:
          uc.actionTypes.add(new KaijuActions.DonkPunchFactory());
        case BASIC:
          break;
      }
    }

    @Override
    public void modifyMovePower(UnitContext uc)
    {
      super.modifyMovePower(uc);
      final KaijuAbilityTier tier = kaijuTracker.kaijuAbilityTier.get(uc.unit);
      // Rampage grants +2 move every four turns
      if( tier == KaijuAbilityTier.ALL
          && kaijuTracker.getTurn() % 4 == 0 )
        uc.movePower += 2;
    }
  } //~BigDonkMod

  public static class Snek extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TANK | LAND | SUBSURFACE;
    public Snek(SnekTunneler tunneler)
    {
      super("Duggemundr", ROLE, SNEK_CHUNKS, SNEK_HPBASES);
      promotesToAtAllSkills = tunneler;
      hasRamSkill = true;
      addUnitModifier(new SnekMod());
    }
  }
  public static class SnekTunneler extends Snek
  {
    private static final long serialVersionUID = 1L;
    public SnekTunneler()
    {
      super(null);
      hidden = true;
      hasDeepTunnelSkill = true;
    }
  }
  public static class SnekMod extends KaijuMoveMod
  {
    private static final long serialVersionUID = 1L;

    KaijuStateTracker kaijuTracker;
    @Override
    public void registerTrackers(GameInstance gi)
    {
      kaijuTracker = StateTracker.instance(gi, KaijuStateTracker.class);
    }
    // No active abilities!

    @Override
    public void modifyUnitDefenseAgainstUnit(BattleParams params)
    {
      if( null == params.map )
        return;
      final Unit snek = params.defender.unit;
      KaijuStateTracker kaijuTracker = StateTracker.instance(params.map.game, KaijuStateTracker.class);
      boolean carapaceReady = kaijuTracker.isReady(snek, SnekMod.class);
      carapaceReady &= kaijuTracker.kaijuAbilityTier.get(snek) != KaijuAbilityTier.BASIC;
      if( carapaceReady ) // Invincible to first hit
        params.defenseSubtraction += 300;
    }

    @Override
    public void modifyMovePower(UnitContext uc)
    {
      super.modifyMovePower(uc);
      // Ram is on by default
      uc.movePower += 1;
    }
  } // ~SnekMod

  public static class UFO extends KaijuUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = JET | AIR_HIGH;
    public UFO(UFOAbducts abductor)
    {
      super("Flying Hubcap", ROLE, UFO_CHUNKS, UFO_HPBASES);
      promotesToAtAllSkills = abductor;
      addUnitModifier(new UFOMod());
    }

    /** Disable my move boost and EMP AoE if I have them and there's a Radar in range */
    @Override
    public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
    {
      GameEventQueue events = super.getTurnInitEvents(self, map);

      KaijuStateTracker kaijuTracker = StateTracker.instance(map.game, KaijuStateTracker.class);
      if( kaijuTracker.kaijuAbilityTier.get(self) != KaijuAbilityTier.BASIC )
      {
        if( isEnemyRadarScanning(map, new XYCoord(self), self.CO) )
        {
          // Setting the tracker state here feels wrong
          // UFO.class is the key for Nav Computer, since there's no active portion
          kaijuTracker.abilityUsedShort(self, UFO.class);
        }
      }
      return events;
    }
  }
  public static class UFOAbducts extends UFO
  {
    private static final long serialVersionUID = 1L;
    public UFOAbducts()
    {
      super(null);
      chargeOnBuildingKill = true;
    }
  }
  public static class UFOMod extends KaijuMoveMod
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
          uc.actionTypes.add(new KaijuActions.UFOEMPFactory());
        case EXTRA:
          uc.actionTypes.add(new KaijuActions.UFOBeamFactory());
        case BASIC:
          break;
      }
    }

    @Override
    public void modifyMovePower(UnitContext uc)
    {
      super.modifyMovePower(uc);
      final KaijuAbilityTier tier = kaijuTracker.kaijuAbilityTier.get(uc.unit);
      // Nav Computer grants +2 move every turn
      if( tier != KaijuAbilityTier.BASIC
          && kaijuTracker.isReady(uc.unit, UFO.class) )
        uc.movePower += 1;
    }
  } //~UFOMod


  public static boolean isEnemyRadarScanning(GameMap map, XYCoord target, Commander affiliation)
  {
    boolean isAnyRadar = false;
    for( XYCoord xyc : Utils.findLocationsInRange(map, target, Radar.VISION_RANGE) )
    {
      Unit resident = map.getResident(xyc);
      if( null != resident && resident.CO.isEnemy(affiliation)
          && (resident.model instanceof Radar) )
      {
        isAnyRadar = true;
        break;
      }
    }
    return isAnyRadar;
  }


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
    public int getTurn() { return game.getCurrentTurn(); }

    // Provide CO energy for studying Kaiju with Radar
    public GameEventQueue receiveMoveEvent(Unit unit, GamePath unitPath)
    {
      if( !(unit.model instanceof Radar) )
        return null;

      int chargeToGive = 0;
      XYCoord end = unitPath.getEndCoord();
      // Check for any hostile kaiju in range to study
      for( Unit kaiju : kaijuAbilityTier.keySet() )
      {
        if( kaiju.CO.isEnemy(unit.CO) && Radar.VISION_RANGE >= end.getDistance(kaiju) )
        {
          chargeToGive += 2;
        }
      }

      if( chargeToGive > 0 )
      {
        GameEventQueue events = new GameEventQueue();
        events.add(new CommanderEnergyChangeEvent(unit.CO, chargeToGive));
        return events;
      }

      return null;
    }

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
      int health = kaijuType.healthBases[tier.ordinal()];
      health += turn;
      int heal = health - UnitModel.MAXIMUM_HEALTH;

      GameEventQueue events = new GameEventQueue();
      events.add(new HealUnitEvent(unit, heal, null, true));

      if( KaijuAbilityTier.ALL == tier && null != kaijuType.promotesToAtAllSkills )
      {
        events.add(new TransformLifecycle.TransformEvent(unit, kaijuType.promotesToAtAllSkills));
      }

      // Make the birb sit if he's low HP
      if( health <= KaijuWarsKaiju.BIRD_LAND_HEALTH )
        tryDevolveHellTurkey(unit, events);

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
    public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer healthBeforeDeath)
    {
      // Stop tracking dead kaiju
      if( victim.model instanceof KaijuUnitModel )
        kaijuAbilityTier.remove(victim);

      UnitModel egg;
      if( victim.model instanceof HellTurkey )
        egg = ((HellTurkey) victim.model).turkeyEgg;
      else if ( victim.model instanceof HellTurkeyLand)
        egg = ((HellTurkeyLand) victim.model).airTurkey.turkeyEgg;
      else // Not a resurrectable Kaiju
        return null;

      if(!isReady(victim, BirdResurrectFactory.class))
        return null; // Can't resurrect since it's on cooldown

      GameEventQueue events = new GameEventQueue();
      boolean unitIsReady = false;
      events.add(new CreateUnitEvent(victim.CO, egg, grave, AnimationStyle.DROP_IN, unitIsReady, FUDGE_RADIUS ));

      return events;
    }
    // Land Hell Turkey if it takes too much damage, and cancel Carapace after combat
    @Override
    public GameEventQueue receiveBattleEvent(BattleSummary summary)
    {
      // Kaiju don't have weapons, so they can't be the attacker.
      final Unit victim = summary.defender.unit;
      if( !(victim.model instanceof KaijuUnitModel) )
        return null;

      if( (victim.model instanceof Snek) )
      {
        abilityUsedShort(victim, SnekMod.class);
      }

      if( summary.defender.after.getHealth() > KaijuWarsKaiju.BIRD_LAND_HEALTH )
        return null;

      GameEventQueue events = new GameEventQueue();
      tryDevolveHellTurkey(victim, events);
      return events;
    }
    // Land Hell Turkey if it takes too much damage
    @Override
    public GameEventQueue receiveMassDamageEvent(Commander attacker, Map<Unit, Integer> lostHealth)
    {
      GameEventQueue events = new GameEventQueue();
      for( Entry<Unit, Integer> pair : lostHealth.entrySet() )
      {
        final Unit victim = pair.getKey();
        if( !(victim.model instanceof KaijuUnitModel) )
          continue;
        if( pair.getValue() + KaijuWarsKaiju.BIRD_LAND_HEALTH < victim.getHealth() )
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
    public static final int KAIJU_ABILITY_COOLDOWN = 5;
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
      int[] healthChunks = kaijuType.healthChunks;
      int healthBudget = uc.getHealth();
      int move = 0;
      while (healthBudget > 0)
      {
        ++move;
        int chunk = DEFAULT_HEALTH_CHUNK;
        if( healthChunks.length > move )
          chunk = healthChunks[move];
        healthBudget -= chunk;
      }
      uc.movePower = move;
    }
  } //~KaijuMoveMod

  public static class FootKaiju extends MoveType
  {
    private static final long serialVersionUID = 1L;
    final boolean stopOnBuildingKill;

    public FootKaiju(boolean stopOnBuildingKill)
    {
      super();
      moveCosts.get(Weathers.CLEAR).setAllMovementCosts(1);
      moveCosts.get(Weathers.RAIN).setAllMovementCosts(1);
      moveCosts.get(Weathers.SNOW).setAllMovementCosts(1);
      moveCosts.get(Weathers.SANDSTORM).setAllMovementCosts(1);

      setMoveCost(TerrainType.TELETILE, 0);
      // 'cause smashing takes work
      setMoveCost(TerrainType.PILLAR, 2);
      setMoveCost(TerrainType.METEOR, 2);
      this.stopOnBuildingKill = stopOnBuildingKill;
    }
    public FootKaiju(FootKaiju other)
    {
      super(other);
      stopOnBuildingKill = other.stopOnBuildingKill;
    }

    @Override
    public MoveType clone()
    {
      return new FootKaiju(this);
    }

    @Override
    public int getTransitionCost(GameMap map, XYCoord from, XYCoord to,
                                 Army team, boolean canTravelThroughEnemies)
    {
      // if we're past the edges of the map
      if( !map.isLocationValid(to) )
        return MoveType.IMPASSABLE;

      int cost = super.getTransitionCost(map, from, to, team, canTravelThroughEnemies);

      // Cannot path through: Kaiju, buildings
      final MapLocation fromLocation = map.getLocation(from);

      if( !canTravelThroughEnemies
          && stopOnBuildingKill
          && fromLocation.isCaptureable()
          && (null == team || team.isEnemy(fromLocation.getOwner())) )
        return MoveType.IMPASSABLE;

      final Unit fromResident = fromLocation.getResident();
      if( null != fromResident && fromResident.CO.isEnemy(team) )
      {
        final KaijuWarsUnitModel fromResidentType = (KaijuWarsUnitModel) fromResident.model;
        if( !canTravelThroughEnemies && null != fromResidentType )
        {
          if( fromResidentType.isKaiju )
            cost = MoveType.IMPASSABLE;
        }
      }

      // Prevent pathing into friendly Kaiju. That doesn't end well.
      final Unit toResident = map.getLocation(to).getResident();
      if( null != toResident && !toResident.CO.isEnemy(team) )
      {
        final KaijuWarsUnitModel toResidentType = (KaijuWarsUnitModel) toResident.model;
        if( !canTravelThroughEnemies && null != toResidentType )
        {
          if( toResidentType.isKaiju )
            cost = MoveType.IMPASSABLE;
        }
      }

      return cost;
    }

    @Override
    public boolean canStandOn(GameMap map, XYCoord end, Unit mover, boolean includeOccupiedDestinations)
    {
      final MapLocation loc = map.getLocation(end);
      if(!canStandOn(loc.getEnvironment()))
        return false;

      return true;
    }
  } // ~KaijuMoveFillFunctor


}
