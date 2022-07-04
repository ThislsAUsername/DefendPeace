package Units;

import java.util.ArrayList;
import Engine.UnitActionFactory;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.HealUnitEvent;
import Engine.GameEvents.ResupplyEvent;
import Engine.UnitActionLifecycles.FlareLifecycle;
import Engine.UnitActionLifecycles.TerraformLifecycle;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Engine.UnitActionLifecycles.UnitProduceLifecycle;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.MoveTypeFey;
import Units.MoveTypes.Tires;
import Units.MoveTypes.TiresRugged;
import Units.MoveTypes.Tread;

public class DoRUnits extends UnitModelScheme
{
  private static final long serialVersionUID = 1L;

  @Override
  public String toString()
  {
    return super.toString() + "DoR";
  }

  @Override
  public String getIconicUnitName()
  {
    return "Infantry";
  }

  @Override
  public GameReadyModels buildGameReadyModels()
  {
    GameReadyModels dorModels = new GameReadyModels();

    ArrayList<UnitModel> factoryModels = new ArrayList<UnitModel>();
    ArrayList<UnitModel> seaportModels = new ArrayList<UnitModel>();
    ArrayList<UnitModel> airportModels = new ArrayList<UnitModel>();

    // Define everything we can build from a Factory.
    factoryModels.add(new InfantryModel());
    factoryModels.add(new MechModel());
    factoryModels.add(new BikeModel());
    factoryModels.add(new ReconModel());
    factoryModels.add(new FlareModel());
    factoryModels.add(new AntiAirModel());
    factoryModels.add(new TankModel());
    factoryModels.add(new MDTankModel());
    factoryModels.add(new WarTankModel());
    factoryModels.add(new ArtilleryModel());
    factoryModels.add(new AntiTankModel());
    factoryModels.add(new RocketsModel());
    factoryModels.add(new MobileSAMModel());
    factoryModels.add(new RigModel());

    // Record those units we can get from a Seaport.
    seaportModels.add(new GunboatModel());
    seaportModels.add(new CruiserModel());
    UnitModel subsub = new SubSubModel(); // Subs are built submerged
    seaportModels.add(subsub);
    UnitModel carrier = new CarrierModel();
    seaportModels.add(carrier);
    seaportModels.add(new BattleshipModel());
    seaportModels.add(new LanderModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new FighterModel());
    airportModels.add(new BomberModel());
    airportModels.add(new DusterModel());
    airportModels.add(new BCopterModel());
    airportModels.add(new TCopterModel());

    // Dump these lists into a hashmap for easy reference later.
    dorModels.shoppingList.put(TerrainType.FACTORY, factoryModels);
    dorModels.shoppingList.put(TerrainType.SEAPORT, seaportModels);
    dorModels.shoppingList.put(TerrainType.AIRPORT, airportModels);

    // Compile one master list of everything we can build.
    for (UnitModel um : factoryModels)
      dorModels.unitModels.add(um);
    for (UnitModel um : seaportModels)
      dorModels.unitModels.add(um);
    for (UnitModel um : airportModels)
      dorModels.unitModels.add(um);

    // Handle transforming units separately, since we don't want two buy-entries
    UnitModel sub = new SubSubModel();
    sub.baseActions.add(new TransformLifecycle.TransformFactory(subsub, "DIVE"));
    subsub.baseActions.add(new TransformLifecycle.TransformFactory(sub, "RISE"));
    dorModels.unitModels.add(sub);

    UnitModel seaplane = new SeaplaneModel();
    carrier.baseActions.add(1, new UnitProduceLifecycle.UnitProduceFactory(seaplane));
    dorModels.unitModels.add(seaplane);

    return dorModels;
  }

  public enum DoRUnitEnum
  {
    INFANTRY, MECH, BIKE,
    RECON, FLARE, ANTI_AIR,
    TANK, MD_TANK, WAR_TANK,
    ARTILLERY, ANTITANK, ROCKETS, MOBILESAM, RIG,
    FIGHTER, BOMBER, SEAPLANE, DUSTER, B_COPTER, T_COPTER,
    GUNBOAT, CRUISER, SUB, SUB_SUB, CARRIER, BATTLESHIP, LANDER,
    METEOR,
  };

  public static final MoveType DoRFloatHeavy = new MoveTypeFey(new FloatHeavy());

  public static class DoRUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public DoRUnitEnum type;

    public DoRUnitModel(String pName, DoRUnitEnum pType, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, UnitActionFactory[] actions, WeaponModel[] weapons, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons, starValue);
      type = pType;
    }
    public DoRUnitModel(String pName, DoRUnitEnum pType, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, ArrayList<UnitActionFactory> actions, ArrayList<WeaponModel> weapons, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons, starValue);
      type = pType;
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      DoRUnitModel newModel = new DoRUnitModel(name, type, role, costBase, maxAmmo, maxFuel, idleFuelBurn, visionRange, baseMovePower,
          baseMoveType.clone(), baseActions, weapons, abilityPowerValue);

      newModel.copyValues(this);
      return newModel;
    }

    @Override
    public double getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }
  }

  public static class InfantryModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;
    
    private static final int UNIT_COST = 1500;
    private static final double STAR_VALUE = 0.4;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = new FootStandard();
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.InfantryMGun() };

    public InfantryModel()
    {
      super("Infantry", DoRUnitEnum.INFANTRY, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class MechModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | MECH | LAND;
    
    private static final int UNIT_COST = 2500;
    private static final double STAR_VALUE = 0.4;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 3;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = new FootMech();
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.MechZooka(), new DoRWeapons.MechMGun() };

    public MechModel()
    {
      super("Mech", DoRUnitEnum.MECH, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class BikeModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | TANK | LAND;
    
    private static final int UNIT_COST = 2500;
    private static final double STAR_VALUE = 0.4;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new TiresRugged();
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.MechMGun() };

    public BikeModel()
    {
      super("Bike", DoRUnitEnum.BIKE, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class ReconModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = RECON | TANK | LAND;
    
    private static final int UNIT_COST = 4000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 80;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 8;

    private static final MoveType moveType = new Tires();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.ReconMGun() };

    public ReconModel()
    {
      super("Recon", DoRUnitEnum.RECON, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class FlareModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = RECON | TANK | LAND;
    
    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 3;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.FlareMGun() };

    public FlareModel()
    {
      super("Flare", DoRUnitEnum.FLARE, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons, STAR_VALUE);
      baseActions.add(0, new FlareLifecycle.FlareFactory(0, 5, 2));
    }
  }

  public static class AntiAirModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SURFACE_TO_AIR | TANK | LAND;
    
    private static final int UNIT_COST = 7000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 6;
    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.AntiAirMGun() };

    public AntiAirModel()
    {
      super("Anti-Air", DoRUnitEnum.ANTI_AIR, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class TankModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 7000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.TankCannon(), new DoRWeapons.TankMGun() };

    public TankModel()
    {
      super("Tank", DoRUnitEnum.TANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
    }
  }

  public static class MDTankModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 5;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.MDTankCannon(), new DoRWeapons.MDTankMGun() };

    public MDTankModel()
    {
      super("Md Tank", DoRUnitEnum.MD_TANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class WarTankModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.6;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 5;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.WarCannon(), new DoRWeapons.WarMGun() };

    public WarTankModel()
    {
      super("War Tank", DoRUnitEnum.WAR_TANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class ArtilleryModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;
    
    private static final int UNIT_COST = 6000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.ArtilleryCannon() };

    public ArtilleryModel()
    {
      super("Artillery", DoRUnitEnum.ARTILLERY, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class AntiTankModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;
    
    private static final int UNIT_COST = 11000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new TiresRugged();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.AntiTankCannon() };

    public AntiTankModel()
    {
      super("AntiTank", DoRUnitEnum.ANTITANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class RocketsModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;
    
    private static final int UNIT_COST = 15000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 5;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tires();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.RocketRockets() };

    public RocketsModel()
    {
      super("Rockets", DoRUnitEnum.ROCKETS, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class MobileSAMModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | SURFACE_TO_AIR | TANK | LAND;
    
    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 5;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 5; // Finally, sigh

    private static final MoveType moveType = new Tires();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.MobileSAMWeapon() };

    public MobileSAMModel()
    {
      super("Missiles", DoRUnitEnum.MOBILESAM, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class RigModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | TANK | LAND;
    
    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 0.8;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;
    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.APC_ACTIONS;

    public RigModel()
    {
      super("APC", DoRUnitEnum.RIG, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
      carryableExclusionMask = TANK; // Can't carry Bikes

      maxMaterials = 1;
      baseActions.add(
          new TerraformLifecycle.TerraformFactory(TerrainType.GRASS, TerrainType.TEMP_AIRPORT, "BUILD"));
      baseActions.add(
          new TerraformLifecycle.TerraformFactory(TerrainType.SHOAL, TerrainType.TEMP_SEAPORT, "BUILD"));
    }
  }

  // air

  public static class FighterModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_AIR | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 20000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 9;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.FighterMissiles() };

    public FighterModel()
    {
      super("Fighter", DoRUnitEnum.FIGHTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class BomberModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | ASSAULT | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 20000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.BomberBombs() };

    public BomberModel()
    {
      super("Bomber", DoRUnitEnum.BOMBER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class SeaplaneModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | AIR_TO_AIR | ASSAULT | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 15000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 40;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 3;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.SeaplaneShots() };

    public SeaplaneModel()
    {
      super("Seaplane", DoRUnitEnum.SEAPLANE, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class DusterModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_AIR | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 13000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 8;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.DusterMGun() };

    public DusterModel()
    {
      super("Duster", DoRUnitEnum.DUSTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class BCopterModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | ASSAULT | HOVER | AIR_LOW;
    
    private static final int UNIT_COST = 9000;
    private static final double STAR_VALUE = 1.2;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 2;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.CopterRockets(), new DoRWeapons.CopterMGun() };

    public BCopterModel()
    {
      super("B-Copter", DoRUnitEnum.B_COPTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class TCopterModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | HOVER | AIR_LOW;
    
    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 2;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.TRANSPORT_ACTIONS;

    public TCopterModel()
    {
      super("T-Copter", DoRUnitEnum.T_COPTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
      carryableExclusionMask = TANK; // Can't carry Bikes
    }
  }

  // sea

  public static class GunboatModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | SHIP | SEA;
    
    private static final int UNIT_COST = 6000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 1;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new FloatLight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.GunBoatGun() };

    public GunboatModel()
    {
      super("Gunboat", DoRUnitEnum.GUNBOAT, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
      carryableExclusionMask = TANK; // Can't carry Bikes
    }
  }

  public static class CruiserModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SURFACE_TO_AIR | TRANSPORT | SHIP | SEA;
    
    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.6;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = DoRFloatHeavy;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.CruiserTorpedoes(), new DoRWeapons.CruiserMGun() };

    public CruiserModel()
    {
      super("Cruiser", DoRUnitEnum.CRUISER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = AIR_LOW;
    }

    /** Cruisers supply their cargo at the beginning of every turn. Make it so. */
    @Override
    public GameEventQueue getTurnInitEvents(Unit self, MapMaster map)
    {
      GameEventQueue events = super.getTurnInitEvents(self, map);
      for( Unit cargo : self.heldUnits )
        if( !cargo.isFullySupplied() )
          events.add(new ResupplyEvent(self, cargo));
      return events;
    }
  }

  public static class SubModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SHIP | SEA;
    
    private static final int UNIT_COST = 20000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = DoRFloatHeavy;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.SubTorpedoes() };

    public SubModel()
    {
      super("Sub", DoRUnitEnum.SUB, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
    }
  }

  public static class SubSubModel extends SubModel
  {
    private static final long serialVersionUID = 1L;
    private static final int IDLE_FUEL_BURN = 5;

    public SubSubModel()
    {
      super();
      type = DoRUnitEnum.SUB_SUB;
      role |= SUBSURFACE;
      idleFuelBurn = IDLE_FUEL_BURN;
      hidden = true;
    }
  }

  public static class CarrierModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SURFACE_TO_AIR | TRANSPORT | SHIP | SEA;
    
    private static final int UNIT_COST = 28000;
    private static final double STAR_VALUE = 2.2;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = DoRFloatHeavy;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.CarrierMGun() };

    public CarrierModel()
    {
      super("Carrier", DoRUnitEnum.CARRIER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      maxMaterials = 4;
      baseCargoCapacity = 2;
      carryableMask = AIR_LOW | AIR_HIGH;
      baseActions.add(0, UnitActionFactory.LAUNCH);
    }

    /** DoR Carriers repair and supply their cargo at the beginning of every turn. Make it so. */
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

  public static class BattleshipModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | SHIP | SEA;
    
    private static final int UNIT_COST = 25000;
    private static final double STAR_VALUE = 2.0;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = DoRFloatHeavy;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new DoRWeapons.BattleshipCannon() };

    public BattleshipModel()
    {
      super("Battleship", DoRUnitEnum.BATTLESHIP, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class LanderModel extends DoRUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | SHIP | SEA;
    
    private static final int UNIT_COST = 10000;
    private static final double STAR_VALUE = 1.2;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new FloatLight();
    private static final UnitActionFactory[] actions = UnitActionFactory.TRANSPORT_ACTIONS;

    public LanderModel()
    {
      super("Lander", DoRUnitEnum.LANDER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK;
    }
  }

}
