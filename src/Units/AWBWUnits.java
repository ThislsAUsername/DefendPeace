package Units;

import java.util.ArrayList;
import Engine.UnitActionFactory;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ResupplyEvent;
import Engine.UnitActionLifecycles.ExplodeLifecycle;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.MoveTypes.Tread;

public class AWBWUnits extends UnitModelScheme
{
  private static final long serialVersionUID = 1L;

  @Override
  public String toString()
  {
    return super.toString() + "AWBW";
  }

  @Override
  public String getIconicUnitName()
  {
    return "Infantry";
  }

  @Override
  public GameReadyModels buildGameReadyModels()
  {
    GameReadyModels awbwModels = new GameReadyModels();

    ArrayList<UnitModel> factoryModels = new ArrayList<UnitModel>();
    ArrayList<UnitModel> seaportModels = new ArrayList<UnitModel>();
    ArrayList<UnitModel> airportModels = new ArrayList<UnitModel>();

    // Define everything we can build from a Factory.
    factoryModels.add(new InfantryModel());
    factoryModels.add(new MechModel());
    factoryModels.add(new APCModel());
    factoryModels.add(new ArtilleryModel());
    factoryModels.add(new ReconModel());
    factoryModels.add(new TankModel());
    factoryModels.add(new MDTankModel());
    factoryModels.add(new NeotankModel());
    factoryModels.add(new MegatankModel());
    factoryModels.add(new RocketsModel());
    factoryModels.add(new AntiAirModel());
    factoryModels.add(new MobileSAMModel());
    factoryModels.add(new PiperunnerModel());

    // Record those units we can get from a Seaport.
    seaportModels.add(new LanderModel());
    seaportModels.add(new CruiserModel());
    UnitModel sub = new SubModel();
    seaportModels.add(sub);
    seaportModels.add(new BattleshipModel());
    seaportModels.add(new CarrierModel());
    seaportModels.add(new BBoatModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new TCopterModel());
    airportModels.add(new BCopterModel());
    airportModels.add(new FighterModel());
    airportModels.add(new BomberModel());
    UnitModel stealth = new StealthModel();
    airportModels.add(stealth);
    airportModels.add(new BBombModel());

    // Dump these lists into a hashmap for easy reference later.
    awbwModels.shoppingList.put(TerrainType.FACTORY, factoryModels);
    awbwModels.shoppingList.put(TerrainType.SEAPORT, seaportModels);
    awbwModels.shoppingList.put(TerrainType.AIRPORT, airportModels);

    // Compile one master list of everything we can build.
    for (UnitModel um : factoryModels)
      awbwModels.unitModels.add(um);
    for (UnitModel um : seaportModels)
      awbwModels.unitModels.add(um);
    for (UnitModel um : airportModels)
      awbwModels.unitModels.add(um);

    // Handle transforming units separately, since we don't want two buy-entries
    UnitModel subsub = new SubSubModel();
    sub.baseActions.add(new TransformLifecycle.TransformFactory(subsub, "DIVE"));
    subsub.baseActions.add(new TransformLifecycle.TransformFactory(sub, "RISE"));
    awbwModels.unitModels.add(subsub);
    UnitModel sneaky = new StealthHideModel();
    stealth.baseActions.add(new TransformLifecycle.TransformFactory(sneaky, "HIDE"));
    sneaky.baseActions.add(new TransformLifecycle.TransformFactory(stealth, "APPEAR"));
    awbwModels.unitModels.add(sneaky);

    return awbwModels;
  }

  public enum AWBWUnitEnum
  {
    INFANTRY, MECH,
    RECON, TANK, MD_TANK, NEOTANK, MEGATANK,
    APC, ARTILLERY, ROCKETS, PIPERUNNER, ANTI_AIR, MOBILESAM,
    FIGHTER, BOMBER, STEALTH, STEALTH_HIDE, B_COPTER, T_COPTER, BBOMB,
    CARRIER, BBOAT, BATTLESHIP, CRUISER, LANDER, SUB, SUB_SUB
  };

  public static class AWBWUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public AWBWUnitEnum type;

    public AWBWUnitModel(String pName, AWBWUnitEnum pType, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, UnitActionFactory[] actions, WeaponModel[] weapons, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons, starValue);
      type = pType;
    }
    public AWBWUnitModel(String pName, AWBWUnitEnum pType, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, ArrayList<UnitActionFactory> actions, ArrayList<WeaponModel> weapons, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons, starValue);
      type = pType;
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      AWBWUnitModel newModel = new AWBWUnitModel(name, type, role, costBase, maxAmmo, maxFuel, idleFuelBurn, visionRange, baseMovePower,
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

  public static class InfantryModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;
    
    private static final int UNIT_COST = 1000;
    private static final double STAR_VALUE = 0.4;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = new FootStandard();
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.InfantryMGun() };

    public InfantryModel()
    {
      super("Infantry", AWBWUnitEnum.INFANTRY, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class MechModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = MECH | TROOP | LAND;
    
    private static final int UNIT_COST = 3000;
    private static final double STAR_VALUE = 0.4;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 3;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = new FootMech();
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MechZooka(), new AWBWWeapons.MechMGun() };

    public MechModel()
    {
      super("Mech", AWBWUnitEnum.MECH, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class APCModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | TANK | LAND;
    
    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 0.8;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;
    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.APC_ACTIONS;

    public APCModel()
    {
      super("APC", AWBWUnitEnum.APC, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
    }
  }

  public static class ReconModel extends AWBWUnitModel
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
    private static final WeaponModel[] weapons = { new AWBWWeapons.ReconMGun() };

    public ReconModel()
    {
      super("Recon", AWBWUnitEnum.RECON, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class TankModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 7000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.TankCannon(), new AWBWWeapons.TankMGun() };

    public TankModel()
    {
      super("Tank", AWBWUnitEnum.TANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons, STAR_VALUE);
    }
  }

  public static class MDTankModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.6;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 8;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MDTankCannon(), new AWBWWeapons.MDTankMGun() };

    public MDTankModel()
    {
      super("Md Tank", AWBWUnitEnum.MD_TANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class NeotankModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 22000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.NeoCannon(), new AWBWWeapons.NeoMGun() };

    public NeotankModel()
    {
      super("Neotank", AWBWUnitEnum.NEOTANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class MegatankModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = ASSAULT | TANK | LAND;
    
    private static final int UNIT_COST = 28000;
    private static final double STAR_VALUE = 2.2;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 3;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MegaCannon(), new AWBWWeapons.MegaMGun() };

    public MegatankModel()
    {
      super("Megatank", AWBWUnitEnum.MEGATANK, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class ArtilleryModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;
    
    private static final int UNIT_COST = 6000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.ArtilleryCannon() };

    public ArtilleryModel()
    {
      super("Artillery", AWBWUnitEnum.ARTILLERY, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class RocketsModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;
    
    private static final int UNIT_COST = 15000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tires();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.RocketRockets() };

    public RocketsModel()
    {
      super("Rockets", AWBWUnitEnum.ROCKETS, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class PiperunnerModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | TANK | LAND;
    
    private static final int UNIT_COST = 20000;
    private static final double STAR_VALUE = 2.0;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 9;

    private static final MoveType moveType = new MoveType();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.PipeGun() };

    public PiperunnerModel()
    {
      super("Piperunner", AWBWUnitEnum.PIPERUNNER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseMoveType.setMoveCost(TerrainType.PILLAR, 1);
      baseMoveType.setMoveCost(TerrainType.METEOR, 1);
      baseMoveType.setMoveCost(TerrainType.FACTORY, 1);
    }
  }

  public static class AntiAirModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SURFACE_TO_AIR | TANK | LAND;
    
    private static final int UNIT_COST = 8000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 6;
    private static final MoveType moveType = new Tread();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.AntiAirMGun() };

    public AntiAirModel()
    {
      super("Anti-Air", AWBWUnitEnum.ANTI_AIR, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class MobileSAMModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | SURFACE_TO_AIR | TANK | LAND;
    
    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.4;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Tires();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MobileSAMWeapon() };

    public MobileSAMModel()
    {
      super("Missiles", AWBWUnitEnum.MOBILESAM, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  // air

  public static class TCopterModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | HOVER | AIR_LOW;
    
    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 2;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.TRANSPORT_ACTIONS;

    public TCopterModel()
    {
      super("T-Copter", AWBWUnitEnum.T_COPTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
    }
  }

  public static class BCopterModel extends AWBWUnitModel
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
    private static final WeaponModel[] weapons = { new AWBWWeapons.CopterRockets(), new AWBWWeapons.CopterMGun() };

    public BCopterModel()
    {
      super("B-Copter", AWBWUnitEnum.B_COPTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class BomberModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | ASSAULT | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 22000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.BomberBombs() };

    public BomberModel()
    {
      super("Bomber", AWBWUnitEnum.BOMBER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class FighterModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_AIR | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 20000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 9;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.FighterMissiles() };

    public FighterModel()
    {
      super("Fighter", AWBWUnitEnum.FIGHTER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class StealthModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = AIR_TO_SURFACE | AIR_TO_AIR | ASSAULT | JET | AIR_HIGH;
    
    private static final int UNIT_COST = 24000;
    private static final double STAR_VALUE = 2.0;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.StealthShots() };

    public StealthModel()
    {
      super("Stealth", AWBWUnitEnum.STEALTH, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class StealthHideModel extends StealthModel
  {
    private static final long serialVersionUID = 1L;
    private static final int IDLE_FUEL_BURN = 8;

    public StealthHideModel()
    {
      super();
      type = AWBWUnitEnum.STEALTH_HIDE;
      idleFuelBurn = IDLE_FUEL_BURN;
      hidden = true;
    }
  }

  public static class BBombModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = JET | AIR_HIGH;
    
    private static final int UNIT_COST = 25000;
    private static final double STAR_VALUE = 0.6;
    private static final int MAX_FUEL = 45;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 9;

    private static final int EXPLODE_RADIUS = 3;
    private static final int EXPLODE_POWER = 5;

    private static final MoveType moveType = new Flight();
    private static final UnitActionFactory[] actions = UnitActionFactory.BASIC_ACTIONS;
    private static final WeaponModel[] weapons = {};

    public BBombModel()
    {
      super("BBomb", AWBWUnitEnum.BBOMB, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseActions.add(0, new ExplodeLifecycle.ExplodeFactory(EXPLODE_POWER, EXPLODE_RADIUS));
    }
  }

  // sea

  public static class BBoatModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | SHIP | SEA;
    
    private static final int UNIT_COST = 7500;
    private static final double STAR_VALUE = 1.0;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new FloatLight();
    private static final UnitActionFactory[] actions = UnitActionFactory.TRANSPORT_ACTIONS;

    public BBoatModel()
    {
      super("BBoat", AWBWUnitEnum.BBOAT, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP;
      baseActions.add(0, UnitActionFactory.REPAIR_UNIT);
    }
  }

  public static class LanderModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TRANSPORT | SHIP | SEA;

    private static final int UNIT_COST = 12000;
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
      super("Lander", AWBWUnitEnum.LANDER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, new WeaponModel[0], STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK;
    }
  }

  public static class SubModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SHIP | SEA;

    private static final int UNIT_COST = 20000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 6;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.SubTorpedoes() };

    public SubModel()
    {
      super("Sub", AWBWUnitEnum.SUB, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
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
      type = AWBWUnitEnum.SUB_SUB;
      role |= SUBSURFACE;
      idleFuelBurn = IDLE_FUEL_BURN;
      hidden = true;
    }
  }

  public static class BattleshipModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | SHIP | SEA;
    
    private static final int UNIT_COST = 28000;
    private static final double STAR_VALUE = 2.2;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.BattleshipCannon() };

    public BattleshipModel()
    {
      super("Battleship", AWBWUnitEnum.BATTLESHIP, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
    }
  }

  public static class CarrierModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | SURFACE_TO_AIR | TRANSPORT | SHIP | SEA;
    
    private static final int UNIT_COST = 30000;
    private static final double STAR_VALUE = 2.2;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.CarrierMissiles() };

    public CarrierModel()
    {
      super("Carrier", AWBWUnitEnum.CARRIER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = AIR_LOW | AIR_HIGH;
    }

    /** Carriers re-supply their cargo at the beginning of every turn. Make it so. */
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

  public static class CruiserModel extends AWBWUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SIEGE | SURFACE_TO_AIR | SHIP | SEA;
    
    private static final int UNIT_COST = 18000;
    private static final double STAR_VALUE = 1.6;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int MAX_AMMO = 9;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.CruiserTorpedoes(), new AWBWWeapons.CruiserMGun() };

    public CruiserModel()
    {
      super("Cruiser", AWBWUnitEnum.CRUISER, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = AIR_LOW;
    }
  }

}
