package Units;

import java.util.ArrayList;
import Engine.UnitActionFactory;
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
    return "Police";
  }

  @Override
  public GameReadyModels buildGameReadyModels()
  {
    GameReadyModels kjwModels = new GameReadyModels();

    ArrayList<UnitModel> factoryModels = new ArrayList<>();
    ArrayList<UnitModel> kaijuModels = new ArrayList<>();
    ArrayList<UnitModel> airportModels = new ArrayList<>();

    // Define everything we can build from a Factory.
    factoryModels.add(new Police());
    factoryModels.add(new Tank());
    factoryModels.add(new Missiles());
    // TODO: infantry
    factoryModels.add(new AA());
    factoryModels.add(new Radar());
    factoryModels.add(new Artillery());
    factoryModels.add(new Maser());
    factoryModels.add(new LigerPanther());
    factoryModels.add(new OGRPlatform());

    // Record those units we can get from a Seaport.
    // TODO

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new Fighter());
    airportModels.add(new Bomber());
    airportModels.add(new Bushplane());
    airportModels.add(new BigBoy());
    airportModels.add(new SuperZ2()); // Does this make sense?

    // Dump these lists into a hashmap for easy reference later.
    kjwModels.shoppingList.put(TerrainType.FACTORY, factoryModels);
    kjwModels.shoppingList.put(TerrainType.SEAPORT, kaijuModels);
    kjwModels.shoppingList.put(TerrainType.AIRPORT, airportModels);

    // Compile one master list of everything we can build.
    for (UnitModel um : factoryModels)
      kjwModels.unitModels.add(um);
    for (UnitModel um : airportModels)
      kjwModels.unitModels.add(um);
    for (UnitModel um : kaijuModels)
      kjwModels.unitModels.add(um);

    // Multiplies all movement (and ranges) by N
    for( UnitModel um : kjwModels.unitModels )
      um.baseMovePower *= KaijuWarsWeapons.KAIJU_SCALE_FACTOR;

    return kjwModels;
  }

  /*
   * Boosts from technologies and tactics; consider making expensive variant units with these boosts

   * tank: +2 counter while on urban
   * missile: +2 ATK while stationary, 1-2 scoot-n-shoot
   * AA: all attacks get +1 damage for each AA next to target, force air units to land on hit
   * inf: +2 counter on ROUGH
   * Radar: +2 science if you end turn within 2 tiles of a Kaiju
   * bomber: enable hitting air for 1 damage
   * fighter: +2 counter on empty(plains?)/water
   * ground: +2 ATK hills, +2 counter on forest/jungle
   * air: +3 move on airport

   * Hover Drives: Gives all ground +1 move and the ability to move on water
   */

  /*
   * Special mechanics

   * slowed by rough terrain: missiles, maser, food cart
   * slow on counter: tank, Liger Panther, AA, OGR(air+ground)
   * Bushplanes put out Crises/fires adjacent to the tile they die
   * Police prevent Crises/fires from showing up within 2 tiles
   * Radar counters certain Kaiju abilities while within 2 tiles
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
    public int kaijuCounter = 1; // Typically = counter damage + 1
    public boolean slowsLand    = false;
    public boolean slowsAir     = false;
    public boolean resistsKaiju = false;
    public boolean isKaiju      = false;

    public KaijuWarsUnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, UnitActionFactory[] actions, WeaponModel[] weapons, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons, starValue);
      needsFuel = false;
    }
    public KaijuWarsUnitModel(String pName, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, ArrayList<UnitActionFactory> actions, ArrayList<WeaponModel> weapons, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons, starValue);
      needsFuel = false;
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      KaijuWarsUnitModel newModel = new KaijuWarsUnitModel(name, role, costBase, maxAmmo, maxFuel, idleFuelBurn, visionRange, baseMovePower,
          baseMoveType.clone(), baseActions, weapons, abilityPowerValue);
      newModel.kaijuCounter = kaijuCounter;
      newModel.slowsLand    = slowsLand;
      newModel.slowsAir     = slowsAir;
      newModel.resistsKaiju = resistsKaiju;
      newModel.isKaiju      = isKaiju;

      newModel.copyValues(this);
      return newModel;
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(1) };

    public Police()
    {
      super("Police", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 1;
    }
  }

  public static class Tank extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(1) };

    public Tank()
    {
      super("Tank", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.Missile() };

    public Missiles()
    {
      super("Missiles", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 0;
    }
  }

  // TODO: infantry

  public static class AA extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SURFACE_TO_AIR | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.AA() };

    public AA()
    {
      super("AA", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 1;
      slowsAir = true;
    }
  }

  // TODO: stun enemies on turn start
  public static class Radar extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = RECON | TANK | LAND;

    private static final int MOVE_POWER = 2;
    private static final int VISION_RANGE = 5;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.ShootAll(1) };

    public Radar()
    {
      super("Radar", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 0;
    }
  }

  public static class Artillery extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;

    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = GROUND;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.Artillery() };

    public Artillery()
    {
      super("Artillery", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.Fighter() };

    public Fighter()
    {
      super("Fighter", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 2;
    }
  }

  public static class Bomber extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | ASSAULT | JET | AIR_HIGH;

    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.Bomber() };

    public Bomber()
    {
      super("Bomber", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 1;
    }
  }

  public static class Bushplane extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | AIR_TO_AIR | JET | AIR_HIGH;

    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.BASIC_ACTIONS;
    private static final WeaponModel[] weapons = {};

    public Bushplane()
    {
      super("Bushplane", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(6) };

    public Maser()
    {
      super("Maser", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.BigBoy() };

    public BigBoy()
    {
      super("Big Boy", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 2;
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(3) };

    public LigerPanther()
    {
      super("Liger Panther", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
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

    private static final MoveType moveType = new Hovercraft();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.SuperZ2() };

    public SuperZ2()
    {
      super("Super Z2", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 4;
    }
  }
  public static class SuperZ2Hurt extends SuperZ2
  {
    private static final long serialVersionUID = 1L;
    public SuperZ2Hurt()
    {
      super();
      name = "Super Z2 hurt";
      kaijuCounter = 1;
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
    private static final WeaponModel[] weapons = { new KaijuWarsWeapons.ShootAll(3) };

    public OGRPlatform()
    {
      super("OGR Platform", ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
      kaijuCounter = 5;
      slowsLand    = true;
      slowsAir     = true;
      resistsKaiju = true;
    }
  }

}
