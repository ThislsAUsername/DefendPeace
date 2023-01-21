package Units;

import java.util.ArrayList;

import Engine.GameInstance;
import Engine.UnitActionFactory;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.StateTrackers.StateTracker;
import Engine.StateTrackers.SuicideAttackTracker;
import Engine.StateTrackers.UnitResurrectionTracker;
import Engine.StateTrackers.UnitTurnPositionTracker;
import Engine.UnitActionLifecycles.UnitProduceLifecycle;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.MoveTypes.Flight;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;

public class KaijuWarsUnits extends UnitModelScheme
{
  private static final long serialVersionUID = 1L;

  @Override
  public String toString()
  {
    return super.toString() + "Kaiju Wars";
  }

  @Override
  public String getIconicUnitName()
  {
    // TODO: Does the infrastructure around this need to change, or should this go away?
    return "Infantry";
  }

  @Override
  public GameReadyModels buildGameReadyModels()
  {
    GameReadyModels kjwModels = new GameReadyModels();

    ArrayList<UnitModel> factoryModels = new ArrayList<>();
    ArrayList<UnitModel> kaijuModels = new ArrayList<>();
    ArrayList<UnitModel> airportModels = new ArrayList<>();
    ArrayList<UnitModel> extras = new ArrayList<>();

    // Define everything we can build from a Factory.
    factoryModels.add(new Police());
    factoryModels.add(new Infantry());
    factoryModels.add(new Tank());
    factoryModels.add(new Missiles());
    factoryModels.add(new Rockets());
    factoryModels.add(new AA());
    factoryModels.add(new Radar());
    factoryModels.add(new Artillery());
    factoryModels.add(new Maser());
    factoryModels.add(new LigerPanther());
    SuperZ2Hurt z2Hurt = new SuperZ2Hurt();
    factoryModels.add(new SuperZ2(z2Hurt));
    extras.add(z2Hurt);
    factoryModels.add(new OGRPlatform());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new Fighter());
    airportModels.add(new Bomber());
    airportModels.add(new Helicopter());
    airportModels.add(new Bushplane());

    // Carrier gets to build all normal air units
    UnitModel carrier = new SkyCarrier();
    // Iterate backwards so the insertion ends up in the right order
    for( int i = airportModels.size() - 1; i >= 0; --i )
      carrier.baseActions.add(1, new UnitProduceLifecycle.UnitProduceFactory(airportModels.get(i)));

    airportModels.add(new BigBoy());
    GuncrossRobot gunBot = new GuncrossRobot();
    airportModels.add(new GuncrossWing(gunBot));
    airportModels.add(new Kaputnik());
    extras.add(gunBot);
    airportModels.add(carrier);

    // Throw in some "bonus units" to show the damage to entrenched inf/fighters
    KaijuWarsUnitModel entrenched = new Infantry();
    entrenched.kaijuCounter += KaijuWarsWeapons.STATIC_INF_BONUS;
    entrenched.hidden        = true;
    KaijuWarsUnitModel divineWind = new Fighter();
    divineWind.kaijuCounter += KaijuWarsWeapons.DIVINE_WIND_BONUS;
    divineWind.hidden        = true;
    extras.add(entrenched);
    extras.add(divineWind);

    // Record those units we can get from a Seaport.
    kaijuModels.add(new KaijuWarsKaiju.Alphazaurus());
    final KaijuWarsKaiju.HellTurkey     turkeyAir  = new KaijuWarsKaiju.HellTurkey();
    final KaijuWarsKaiju.HellTurkeyLand turkeyLand = new KaijuWarsKaiju.HellTurkeyLand(turkeyAir);
    final KaijuWarsKaiju.HellTurkeyEgg  turkeyEgg  = new KaijuWarsKaiju.HellTurkeyEgg(turkeyLand);
    turkeyAir.turkeyLand = turkeyLand;
    turkeyAir.turkeyEgg = turkeyEgg;
    kaijuModels.add(turkeyAir);
    extras.add(turkeyLand);
    extras.add(turkeyEgg);
    final KaijuWarsKaiju.BigDonkRampage rampager = new KaijuWarsKaiju.BigDonkRampage();
    extras.add(rampager);
    kaijuModels.add(new KaijuWarsKaiju.BigDonk(rampager));
    final KaijuWarsKaiju.SnekTunneler tunneler = new KaijuWarsKaiju.SnekTunneler();
    extras.add(tunneler);
    kaijuModels.add(new KaijuWarsKaiju.Snek(tunneler));
    final KaijuWarsKaiju.UFOAbducts abductor = new KaijuWarsKaiju.UFOAbducts();
    extras.add(abductor);
    kaijuModels.add(new KaijuWarsKaiju.UFO(abductor));

    // Dump these lists into a hashmap for easy reference later.
    kjwModels.shoppingList.put(TerrainType.FACTORY, factoryModels);
    kjwModels.shoppingList.put(TerrainType.AIRPORT, airportModels);
    kjwModels.shoppingList.put(TerrainType.SEAPORT, kaijuModels);

    // Compile one master list of everything we can build.
    for (UnitModel um : factoryModels)
      kjwModels.unitModels.add(um);
    for (UnitModel um : airportModels)
      kjwModels.unitModels.add(um);
    for (UnitModel um : extras)
      kjwModels.unitModels.add(um);
    for (UnitModel um : kaijuModels)
      kjwModels.unitModels.add(um);

    return kjwModels;
  }

  public void registerStateTrackers(GameInstance gi)
  {
    super.registerStateTrackers(gi);

    StateTracker.instance(gi, UnitTurnPositionTracker.class);

    UnitResurrectionTracker rezzer = StateTracker.instance(gi, UnitResurrectionTracker.class);
    // Populate resurrection pairs
    GameReadyModels grms = gi.rules.unitModelScheme.getGameReadyModels();
    for( UnitModel um : grms.unitModels )
    {
      KaijuWarsUnitModel umCast = (KaijuWarsUnitModel) um;
      if( null == umCast.resurrectsAs )
        continue;
      rezzer.resurrectionTypeMap.put(um, umCast.resurrectsAs);
    }

    SuicideAttackTracker kaputnikTracker = StateTracker.instance(gi, SuicideAttackTracker.class);
    for( UnitModel um : grms.unitModels )
    {
      KaijuWarsUnitModel umCast = (KaijuWarsUnitModel) um;
      if( !umCast.suicideAttack )
        continue;

      kaputnikTracker.killTypeMap.add(um);
    }
  }

  /*
   * Units I do not plan to implement at this time

   * Freezer (0-counter unit with 1 damage that slows targets by a tile; too complicated and bad)
   * Food Cart (attracts kaiju within 3 tiles; not implementing kaiju path/targeting mechanics)
   * Shark Jet (faster Fighter that moves after shooting; messy and either OP or UP)
   * Cannon of Boom (immobile Rocket equivalent that teleports between labs; ew)
   */

  /*
   * Boosts from technologies and tactics; consider making expensive variant units with these boosts

   * tank: +2 counter while on urban
   * missile: +2 ATK while stationary, 1-2 scoot-n-shoot
   *  - Giving them the former
   *  - The latter is hilariously OP, so let's make an "uber missile" that does that?
   * AA: all attacks get +1 damage for each AA next to target, force air units to land on hit
   *  - Added the +1; the other is too jank
   * inf: +2 counter on ROUGH terrain
   * Radar: +2 science if you end turn within 2 tiles of a Kaiju
   *  - Should implement as +2 CO energy, but only for opposing Kaiju
   * bomber: enable hitting air for 1 damage
   * fighter: +2 counter on empty(plains?)/water
   *  - Adding
   * ground: +2 ATK hills, +2 counter on forest/jungle
   *  - Adding the first
   * air: +3 move on airport
   *  - Adding

   * Hover Drives: Gives all ground +1 move and the ability to move on water (Super Z2 doesn't benefit)
   * Ultimate Autobahn: Gives all ground +2 move if starting on road/bridge
   */

  /*
   * Special mechanics

   * slowed by rough terrain: missiles, maser, food cart
   * slow on counter: tank, Liger Panther, AA, OGR(air+ground)
   * +2 counter if you don't move: inf
   * Bushplanes put out Crises/fires adjacent to the tile they die
   *  - Now are capture units
   * Police prevent Crises/fires from showing up within 2 tiles
   *  - Now are capture units
   * Radar counters certain Kaiju abilities while within 2 tiles
   *  - piercing vision, will add those
   * Copters and Sky Carrier give +1 ATK to adjacent allies

   * Freezer slows on attack
   * Guncross Wing becomes Guncross Robot on death
   * Super Z2 also lives on death, but just goes from 4 to 1 counter power
   * Super Z2 can go on water, but is a land unit for all other purposes
   * Kaputnik has a suicide attack
   * Shark Jet can move (but not shoot) again after shooting
   * Sky Carrier lets you build air units adjacent to it
   * Cannon Of Boom cannot move normally, but can teleport between labs as its turn
   */

  public static class KaijuWarsUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public int kaijuCounter = 0;
    public boolean entrenches     = false;
    public boolean stillBoost     = false;
    public boolean divineWind     = false; // +2 counter on plains/sea
    public boolean boostsAllies   = false;
    public boolean boostSurround  = false;

    public UnitModel resurrectsAs = null;
    public boolean suicideAttack  = false;

    public boolean slowsLand      = false;
    public boolean slowsAir       = false;
    public boolean resistsKaiju   = false;
    public boolean isKaiju        = false;

    public KaijuWarsUnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, UnitActionFactory[] actions, WeaponModel[] WEAPONS, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, WEAPONS, starValue);
      fuelBurnPerTile = 0;
      needsMaterials = false;
      addUnitModifier(new KaijuWarsWeapons.KaijuWarsFightMod());
    }
    public KaijuWarsUnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, ArrayList<UnitActionFactory> actions, ArrayList<WeaponModel> WEAPONS, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, WEAPONS, starValue);
      fuelBurnPerTile = 0;
      needsMaterials = false;
      addUnitModifier(new KaijuWarsWeapons.KaijuWarsFightMod());
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      KaijuWarsUnitModel newModel = new KaijuWarsUnitModel(name, role, costBase, maxAmmo, maxFuel, fuelBurnIdle, visionRange, baseMovePower,
          baseMoveType.clone(), baseActions, weapons, abilityPowerValue);

      newModel.copyValues(this);
      return newModel;
    }
    public void copyValues(KaijuWarsUnitModel other)
    {
      super.copyValues(other);
      kaijuCounter  = other.kaijuCounter;
      entrenches    = other.entrenches;
      stillBoost    = other.stillBoost;
      boostsAllies  = other.boostsAllies;
      boostSurround = other.boostSurround;

      resurrectsAs  = other.resurrectsAs;
      suicideAttack = other.suicideAttack;

      slowsLand     = other.slowsLand;
      slowsAir      = other.slowsAir;
      resistsKaiju  = other.resistsKaiju;
      isKaiju       = other.isKaiju;
    }

    @Override
    public double getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }
  }

  // These go for all units that aren't experiments
  private static final int UNIT_COST = 3000;
  private static final double STAR_VALUE = 1.0;
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int MAX_AMMO = -1;
  private static final int VISION_RANGE = 2;
  private static final MoveType GROUND = new FootMech();
  private static final MoveType AWKWARD = new FootAwkward();
  public static class FootAwkward extends Tread
  {
    private static final long serialVersionUID = 1L;

    public FootAwkward()
    {
      super();
      setMoveCost(TerrainType.MOUNTAIN, 2);
      setMoveCost(Weathers.RAIN, TerrainType.MOUNTAIN, 3);
    }
  }
  public static class Hovercraft extends FootMech
  {
    private static final long serialVersionUID = 1L;

    public Hovercraft()
    {
      super();
      moveCosts.get(Weathers.CLEAR).setAllSeaCosts(1);
      moveCosts.get(Weathers.RAIN).setAllSeaCosts(1);
      moveCosts.get(Weathers.SNOW).setAllSeaCosts(1);
      moveCosts.get(Weathers.SANDSTORM).setAllSeaCosts(1);

      setMoveCost(TerrainType.REEF, 2);
      setMoveCost(Weathers.SNOW, TerrainType.SEA, 2);
    }
  }

  public static class Police extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;

    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.ShootLand(1) };

    public Police()
    {
      super("Police", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 1;
    }
  }

  public static class Infantry extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;

    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.ShootLand(1) };

    public Infantry()
    {
      super("Infantry", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 1;
      entrenches = true;
    }
  }

  public static class Tank extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.ShootLand(1) };

    public Tank()
    {
      super("Tank", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 2;
      slowsLand = true;
    }
  }

  public static class Missiles extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    // Mech because they're relatively slow and relatively strong
    private static final long ROLE = MECH | SURFACE_TO_AIR | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = AWKWARD;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.Missile() };

    public Missiles()
    {
      super("Missiles", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 0;
      stillBoost = true;
    }
  }
  public static class Rockets extends Missiles
  {
    private static final long serialVersionUID = 1L;
    public Rockets()
    {
      super();
      name = "Rockets";
      costBase = UNIT_COST * 3;
      weapons.clear();
      weapons.add(new KaijuWarsWeapons.Rockets());
    }
  }

  public static class AA extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SURFACE_TO_AIR | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.AA() };

    public AA()
    {
      super("Anti-Air", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 1;
      slowsAir = true;
      boostSurround = true;
    }
  }

  public static class Radar extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = RECON | TANK | LAND;

    private static final int MOVE_POWER = 2;
    // For spotting tunneling kaiju
    public static final int PIERCING_VISION = 2;
    private static final int VISION_RANGE = 5;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.ShootAll(1) };

    public Radar()
    {
      super("Radar", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 0;
      visionRangePiercing = PIERCING_VISION;
    }
  }

  public static class Artillery extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.Artillery() };

    public Artillery()
    {
      super("Artillery", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 0;
    }
  }

  // air

  public static class Fighter extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | AIR_TO_AIR | JET | AIR_HIGH;

    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.Fighter() };

    public Fighter()
    {
      super("Fighter", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 2;
      divineWind   = true;
    }
  }

  public static class Bomber extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | ASSAULT | JET | AIR_HIGH;

    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.Bomber() };

    public Bomber()
    {
      super("Bomber", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 1;
    }
  }

  public static class Helicopter extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = HOVER | AIR_LOW;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.BASIC_ACTIONS;
    private static final WeaponModel[] WEAPONS = {};

    public Helicopter()
    {
      super("Helicopter", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 1;
      boostsAllies = true;
    }
  }

  public static class Bushplane extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = JET | AIR_HIGH;

    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.BASIC_ACTIONS;
    private static final WeaponModel[] WEAPONS = {};

    public Bushplane()
    {
      super("Bushplane", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 0;
      baseActions.add(0, UnitActionFactory.CAPTURE);
    }
  }

  // Experimental weapons

  // Cost per turn of prep the experimental weapon normally takes
  private static final int PREP_COST = 3000;

  public static class Maser extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = MECH | TANK | LAND;

    private static final int MOVE_POWER = 2;
    private static final int UNIT_COST = PREP_COST * 4;

    private static final MoveType moveType = AWKWARD;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.Maser() };

    public Maser()
    {
      super("Maser", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 0;
    }
  }

  public static class BigBoy extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | ASSAULT | JET | AIR_HIGH;

    private static final int MOVE_POWER = 3;
    private static final int UNIT_COST = PREP_COST * 3;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.BigBoy() };

    public BigBoy()
    {
      super("Big Boy", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 2;
    }
  }

  public static class SkyCarrier extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = HOVER | AIR_HIGH | TRANSPORT;

    private static final int MOVE_POWER = 3;
    private static final int UNIT_COST = PREP_COST * 7; // Consider reducing this since this version is nerfed

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.BASIC_ACTIONS;
    private static final WeaponModel[] WEAPONS = {};

    public SkyCarrier()
    {
      super("Sky Carrier", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      baseCargoCapacity = 4;
      carryableMask = AIR_LOW | AIR_HIGH;
      // Let's allow stacking carriers, because why not?
      // carryableExclusionMask = TRANSPORT;
      baseActions.add(0, UnitActionFactory.LAUNCH);

      kaijuCounter = 5;
      boostsAllies = true;
    }

    /** Repair cargo to (badly) simulate repairing lots of dudes in the source game */
    @Override
    public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
    {
      GameEventQueue events = super.getTurnInitEvents(self, map);
      for( Unit cargo : self.heldUnits )
      {
        events.add(new HealUnitEvent(cargo, self.CO.getRepairPower(), self.CO.army)); // Event handles cost logic
        if( !cargo.isFullySupplied() )
          events.add(new ResupplyEvent(self, cargo));
      }
      return events;
    }
  }

  public static class Kaputnik extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | AIR_TO_AIR | JET | AIR_HIGH;

    private static final int MOVE_POWER = 8;
    private static final int UNIT_COST = PREP_COST * 5;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.Kaputnik() };

    public Kaputnik()
    {
      super("Kaputnik", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter  = 0;
      suicideAttack = true;
    }
  }

  public static class LigerPanther extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;

    private static final int MOVE_POWER = 3;
    private static final int UNIT_COST = PREP_COST * 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.ShootLand(3) };

    public LigerPanther()
    {
      super("Liger Panther", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 3;
      slowsLand = true;
    }
  }

  public static class SuperZ2 extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | SURFACE_TO_AIR | TANK | LAND;

    private static final int MOVE_POWER = 3;
    private static final int UNIT_COST = PREP_COST * 4;
    static final String REZ_TO_NAME = "Super Z2 hurt";

    private static final MoveType moveType = new Hovercraft();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.SuperZ2() };

    public SuperZ2(UnitModel rezTo)
    {
      super("Super Z2", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 4;
      resurrectsAs = rezTo;
    }
  }
  public static class SuperZ2Hurt extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | SURFACE_TO_AIR | TANK | LAND;

    private static final int MOVE_POWER = 3;
    private static final int UNIT_COST = PREP_COST * 1;

    private static final MoveType moveType = new Hovercraft();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.SuperZ2() };

    public SuperZ2Hurt()
    {
      super("Super Z2 hurt", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 1;
    }
  }

  public static class GuncrossWing extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | AIR_TO_AIR | JET | AIR_HIGH;

    private static final int MOVE_POWER = 5;
    private static final int UNIT_COST = PREP_COST * 5;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.GuncrossWing() };

    public GuncrossWing(UnitModel rezTo)
    {
      super("Guncross Wing", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 3;
      resurrectsAs = rezTo;
    }
  }
  public static class GuncrossRobot extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TANK | ASSAULT | LAND;

    private static final int MOVE_POWER = 3;
    private static final int UNIT_COST = PREP_COST * 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.GuncrossRobot() };

    public GuncrossRobot()
    {
      super("Guncross Robot", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 2;
    }
  }

  public static class OGRPlatform extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | SURFACE_TO_AIR | TANK | LAND;

    private static final int MOVE_POWER = 2;
    private static final int UNIT_COST = PREP_COST * 4;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] WEAPONS = { new KaijuWarsWeapons.ShootAll(3) };

    public OGRPlatform()
    {
      super("OGR Platform", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, WEAPONS, STAR_VALUE);
      kaijuCounter = 5;
      slowsLand    = true;
      slowsAir     = true;
      resistsKaiju = true;
    }
  }

}
