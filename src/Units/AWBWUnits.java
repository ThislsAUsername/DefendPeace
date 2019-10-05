package Units;

import java.util.ArrayList;
import java.util.Vector;
import Engine.GameAction;
import Engine.UnitActionType;
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
  @Override
  public GameReadyModels getGameReadyModels()
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
    seaportModels.add(new SubModel());
    seaportModels.add(new BattleshipModel());
    seaportModels.add(new CarrierModel());
    seaportModels.add(new BBoatModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new TCopterModel());
    airportModels.add(new BCopterModel());
    airportModels.add(new FighterModel());
    airportModels.add(new BomberModel());
    airportModels.add(new StealthModel());
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
    awbwModels.unitModels.add(subsub);
    UnitModel stealthy = new StealthHideModel();
    awbwModels.unitModels.add(stealthy);

    return awbwModels;
  }

  public static class InfantryModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 1000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 3;

    private static final MoveType moveType = new FootStandard();
    private static final UnitActionType[] actions = UnitActionType.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.InfantryMGun() };

    public InfantryModel()
    {
      super("Infantry", UnitEnum.INFANTRY, ChassisEnum.TROOP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class MechModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 3000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 2;

    private static final MoveType moveType = new FootMech();
    private static final UnitActionType[] actions = UnitActionType.FOOTSOLDIER_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MechZooka(), new AWBWWeapons.MechMGun() };

    public MechModel()
    {
      super("Mech", Units.UnitModel.UnitEnum.MECH, ChassisEnum.TROOP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons);
    }
  }

  public static class APCModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 5000;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;
    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.APC_ACTIONS;

    public APCModel()
    {
      super("APC", UnitEnum.APC, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0]);
      holdingCapacity = 1;
      ChassisEnum[] carryable = { ChassisEnum.TROOP };
      holdables = new Vector<ChassisEnum>(carryable.length);
      for( int i = 0; i < holdables.capacity(); i++ )
      {
        holdables.add(carryable[i]);
      }
    }

    /**
     * APCs re-supply any adjacent allies at the beginning of every turn. Make it so.
     */
    @Override
    public ArrayList<GameAction> getTurnInitActions(Unit self)
    {
      ArrayList<GameAction> actions = new ArrayList<GameAction>(1);
      actions.add(new GameAction.ResupplyAction(self));
      return actions;
    }
  }

  public static class ReconModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 4000;
    private static final int MAX_FUEL = 80;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 8;

    private static final MoveType moveType = new Tires();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.ReconMGun() };

    public ReconModel()
    {
      super("Recon", Units.UnitModel.UnitEnum.RECON, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons);
    }
  }

  public static class TankModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 7000;
    private static final int MAX_FUEL = 70;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.TankCannon(), new AWBWWeapons.TankMGun() };

    public TankModel()
    {
      super("Tank", UnitEnum.TANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons);
    }
  }

  public static class MDTankModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 16000;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MDTankCannon(), new AWBWWeapons.MDTankMGun() };

    public MDTankModel()
    {
      super("Medium Tank", UnitEnum.MD_TANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class NeotankModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 22000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.NeoCannon(), new AWBWWeapons.NeoMGun() };

    public NeotankModel()
    {
      super("Neotank", UnitEnum.NEOTANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class MegatankModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 28000;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MegaCannon(), new AWBWWeapons.MegaMGun() };

    public MegatankModel()
    {
      super("Mega Tank", UnitEnum.MEGATANK, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class ArtilleryModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 6000;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.ArtilleryCannon() };

    public ArtilleryModel()
    {
      super("Artillery", UnitEnum.ARTILLERY, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class RocketsModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 15000;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new Tires();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.RocketRockets() };

    public RocketsModel()
    {
      super("Rockets", UnitEnum.ROCKETS, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class PiperunnerModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 20000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 9;

    private static final MoveType moveType = new MoveType();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.PipeGun() };

    public PiperunnerModel()
    {
      super("Piperunner", UnitEnum.PIPERUNNER, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      propulsion.setMoveCost(TerrainType.PILLAR, 1);
      propulsion.setMoveCost(TerrainType.FACTORY, 1);
    }
  }

  public static class AntiAirModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 8000;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 6;
    private static final MoveType moveType = new Tread();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.AntiAirMGun() };

    public AntiAirModel()
    {
      super("Anti-Air", UnitEnum.ANTI_AIR, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class MobileSAMModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 12000;
    private static final int MAX_FUEL = 50;
    private static final int IDLE_FUEL_BURN = 0;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 4;

    private static final MoveType moveType = new Tires();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.MobileSAMWeapon() };

    public MobileSAMModel()
    {
      super("Mobile SAM", UnitEnum.MOBILESAM, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  // air

  public static class TCopterModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 5000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 2;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionType[] actions = UnitActionType.TRANSPORT_ACTIONS;

    public TCopterModel()
    {
      super("T Copter", UnitEnum.T_COPTER, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, new WeaponModel[0]);
      holdingCapacity = 1;
      ChassisEnum[] carryable = { ChassisEnum.TROOP };
      holdables = new Vector<ChassisEnum>(carryable.length);
      for( int i = 0; i < holdables.capacity(); i++ )
      {
        holdables.add(carryable[i]);
      }
    }
  }

  public static class BCopterModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 9000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 2;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.CopterRockets(), new AWBWWeapons.CopterMGun() };

    public BCopterModel()
    {
      super("B Copter", UnitEnum.B_COPTER, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class BomberModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 22000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new Flight();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.BomberBombs() };

    public BomberModel()
    {
      super("Bomber", UnitEnum.BOMBER, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class FighterModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 20000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 9;

    private static final MoveType moveType = new Flight();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.FighterMissiles() };

    public FighterModel()
    {
      super("Fighter", UnitEnum.FIGHTER, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class StealthModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 24000;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new Flight();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.StealthShots() };

    public StealthModel()
    {
      super("Stealth", UnitEnum.STEALTH, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      addStealthAction();
    }

    protected void addStealthAction()
    {
      possibleActions.add(new UnitActionType.Transform(UnitEnum.STEALTH_HIDE, "HIDE"));
    }
  }

  public static class StealthHideModel extends StealthModel
  {
    private static final long serialVersionUID = 1L;
    private static final int IDLE_FUEL_BURN = 8;

    public StealthHideModel()
    {
      super();
      type = UnitEnum.STEALTH_HIDE;
      idleFuelBurn = IDLE_FUEL_BURN;
      hidden = true;
    }

    @Override
    protected void addStealthAction()
    {
      possibleActions.add(new UnitActionType.Transform(UnitEnum.STEALTH, "APPEAR"));
    }
  }

  public static class BBombModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 25000;
    private static final int MAX_FUEL = 45;
    private static final int IDLE_FUEL_BURN = 5;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 9;

    private static final MoveType moveType = new Flight();
    private static final UnitActionType[] actions = UnitActionType.BASIC_ACTIONS;
    private static final WeaponModel[] weapons = {};

    public BBombModel()
    {
      super("BBomb", UnitEnum.BBOMB, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      possibleActions.add(new UnitActionType.Explode(5, 3));
    }
  }

  // sea

  public static class BBoatModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 7500;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 7;

    private static final MoveType moveType = new FloatLight();
    private static final UnitActionType[] actions = UnitActionType.TRANSPORT_ACTIONS;

    public BBoatModel()
    {
      super("BBoat", UnitEnum.BBOAT, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0]);
      holdingCapacity = 2;
      ChassisEnum[] carryable = { ChassisEnum.TROOP };
      holdables = new Vector<ChassisEnum>(carryable.length);
      for( int i = 0; i < holdables.capacity(); i++ )
      {
        holdables.add(carryable[i]);
      }
      possibleActions.add(UnitActionType.REPAIR_UNIT);
    }
  }

  public static class LanderModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 12000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int VISION_RANGE = 1;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new FloatLight();
    private static final UnitActionType[] actions = UnitActionType.TRANSPORT_ACTIONS;

    public LanderModel()
    {
      super("Lander", UnitEnum.LANDER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, new WeaponModel[0]);
      holdingCapacity = 2;
      ChassisEnum[] carryable = { ChassisEnum.TROOP, ChassisEnum.TANK };
      holdables = new Vector<ChassisEnum>(carryable.length);
      for( int i = 0; i < holdables.capacity(); i++ )
      {
        holdables.add(carryable[i]);
      }
    }
  }

  public static class SubModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 20000;
    private static final int MAX_FUEL = 60;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int VISION_RANGE = 5;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.SubTorpedoes() };

    public SubModel()
    {
      super("Submarine", UnitEnum.SUB, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons);
      addSubAction();
    }

    protected void addSubAction()
    {
      possibleActions.add(new UnitActionType.Transform(UnitEnum.SUB_SUB, "DIVE"));
    }
  }

  public static class SubSubModel extends SubModel
  {
    private static final long serialVersionUID = 1L;
    private static final int IDLE_FUEL_BURN = 5;

    public SubSubModel()
    {
      super();
      type = UnitEnum.SUB_SUB;
      chassis = ChassisEnum.SUBMERGED;
      idleFuelBurn = IDLE_FUEL_BURN;
      hidden = true;
    }

    @Override
    protected void addSubAction()
    {
      possibleActions.add(new UnitActionType.Transform(UnitEnum.SUB, "RISE"));
    }
  }

  public static class BattleshipModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 28000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int VISION_RANGE = 2;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_VEHICLE_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.BattleshipCannon() };

    public BattleshipModel()
    {
      super("Battleship", UnitEnum.BATTLESHIP, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class CarrierModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 30000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int VISION_RANGE = 4;
    private static final int MOVE_POWER = 5;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_TRANSPORT_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.CarrierMissiles() };

    public CarrierModel()
    {
      super("Carrier", UnitEnum.CARRIER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      holdingCapacity = 2;
      ChassisEnum[] carryable = { ChassisEnum.AIR_LOW, ChassisEnum.AIR_HIGH };
      holdables = new Vector<ChassisEnum>(carryable.length);
      for( int i = 0; i < holdables.capacity(); i++ )
      {
        holdables.add(carryable[i]);
      }
    }
  }

  public static class CruiserModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int UNIT_COST = 18000;
    private static final int MAX_FUEL = 99;
    private static final int IDLE_FUEL_BURN = 1;
    private static final int VISION_RANGE = 3;
    private static final int MOVE_POWER = 6;

    private static final MoveType moveType = new FloatHeavy();
    private static final UnitActionType[] actions = UnitActionType.COMBAT_TRANSPORT_ACTIONS;
    private static final WeaponModel[] weapons = { new AWBWWeapons.CruiserTorpedoes(), new AWBWWeapons.CruiserMGun() };

    public CruiserModel()
    {
      super("Cruiser", UnitEnum.CRUISER, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      holdingCapacity = 2;
      ChassisEnum[] carryable = { ChassisEnum.AIR_LOW };
      holdables = new Vector<ChassisEnum>(carryable.length);
      for( int i = 0; i < holdables.capacity(); i++ )
      {
        holdables.add(carryable[i]);
      }
    }
  }

}
