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
  private static final long serialVersionUID = 1L;

  @Override
  public String toString()
  {
    return "AWBW";
  }

  @Override
  public String getIconicUnitName()
  {
    return "Infantry";
  }

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
    sub.possibleActions.add(new UnitActionType.Transform(subsub, "DIVE"));
    subsub.possibleActions.add(new UnitActionType.Transform(sub, "RISE"));
    awbwModels.unitModels.add(subsub);
    UnitModel sneaky = new StealthHideModel();
    stealth.possibleActions.add(new UnitActionType.Transform(sneaky, "HIDE"));
    sneaky.possibleActions.add(new UnitActionType.Transform(stealth, "APPEAR"));
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

    public AWBWUnitModel(String pName, AWBWUnitEnum pType, UnitRoleEnum pRole, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, UnitActionType[] actions, WeaponModel[] weapons)
    {
      super(pName, pRole, pChassis, cost, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons);
      type = pType;
    }
    public AWBWUnitModel(String pName, AWBWUnitEnum pType, UnitRoleEnum pRole, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, ArrayList<UnitActionType> actions, ArrayList<WeaponModel> weapons)
    {
      super(pName, pRole, pChassis, cost, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, weapons);
      type = pType;
    }

    @Override
    public UnitModel clone()
    {
      // Make a copy of your weapon types.
      if( weaponModels != null )
      {
        weaponModels = new ArrayList<WeaponModel>();
        for( WeaponModel weapon : weaponModels )
        {
          weaponModels.add(weapon.clone());
        }
      }

      // Create a new model with the given attributes.
      AWBWUnitModel newModel = new AWBWUnitModel(name, type, role, chassis, getCost(), maxFuel, idleFuelBurn, visionRange, movePower,
          new MoveType(propulsion), possibleActions, weaponModels);

      // Duplicate the other model's transporting abilities.
      newModel.holdingCapacity = holdingCapacity;
      newModel.holdables.addAll(holdables);

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
      super("Infantry", AWBWUnitEnum.INFANTRY, UnitRoleEnum.INFANTRY, ChassisEnum.TROOP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class MechModel extends AWBWUnitModel
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
      super("Mech", AWBWUnitEnum.MECH, UnitRoleEnum.MECH, ChassisEnum.TROOP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons);
    }
  }

  public static class APCModel extends AWBWUnitModel
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
      super("APC", AWBWUnitEnum.APC, UnitRoleEnum.TRANSPORT, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
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

  public static class ReconModel extends AWBWUnitModel
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
      super("Recon", AWBWUnitEnum.RECON, UnitRoleEnum.RECON, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE,
          MOVE_POWER, moveType, actions, weapons);
    }
  }

  public static class TankModel extends AWBWUnitModel
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
      super("Tank", AWBWUnitEnum.TANK, UnitRoleEnum.ASSAULT, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons);
    }
  }

  public static class MDTankModel extends AWBWUnitModel
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
      super("Md Tank", AWBWUnitEnum.MD_TANK, UnitRoleEnum.ASSAULT, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class NeotankModel extends AWBWUnitModel
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
      super("Neotank", AWBWUnitEnum.NEOTANK, UnitRoleEnum.ASSAULT, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class MegatankModel extends AWBWUnitModel
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
      super("Megatank", AWBWUnitEnum.MEGATANK, UnitRoleEnum.ASSAULT, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class ArtilleryModel extends AWBWUnitModel
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
      super("Artillery", AWBWUnitEnum.ARTILLERY, UnitRoleEnum.SIEGE, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class RocketsModel extends AWBWUnitModel
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
      super("Rockets", AWBWUnitEnum.ROCKETS, UnitRoleEnum.SIEGE, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class PiperunnerModel extends AWBWUnitModel
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
      super("Piperunner", AWBWUnitEnum.PIPERUNNER, UnitRoleEnum.SIEGE, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      propulsion.setMoveCost(TerrainType.PILLAR, 1);
      propulsion.setMoveCost(TerrainType.FACTORY, 1);
    }
  }

  public static class AntiAirModel extends AWBWUnitModel
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
      super("Anti-Air", AWBWUnitEnum.ANTI_AIR, UnitRoleEnum.ANTI_AIR, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class MobileSAMModel extends AWBWUnitModel
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
      super("Mobile SAM", AWBWUnitEnum.MOBILESAM, UnitRoleEnum.ANTI_AIR, ChassisEnum.TANK, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  // air

  public static class TCopterModel extends AWBWUnitModel
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
      super("T-Copter", AWBWUnitEnum.T_COPTER, UnitRoleEnum.TRANSPORT, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
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

  public static class BCopterModel extends AWBWUnitModel
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
      super("B-Copter", AWBWUnitEnum.B_COPTER, UnitRoleEnum.AIR_ASSAULT, ChassisEnum.AIR_LOW, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class BomberModel extends AWBWUnitModel
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
      super("Bomber", AWBWUnitEnum.BOMBER, UnitRoleEnum.AIR_ASSAULT, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class FighterModel extends AWBWUnitModel
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
      super("Fighter", AWBWUnitEnum.FIGHTER, UnitRoleEnum.AIR_SUPERIORITY, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class StealthModel extends AWBWUnitModel
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
      super("Stealth", AWBWUnitEnum.STEALTH, UnitRoleEnum.AIR_ASSAULT, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
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
      super("BBomb", AWBWUnitEnum.BBOMB, UnitRoleEnum.AIR_ASSAULT, ChassisEnum.AIR_HIGH, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
      possibleActions.add(new UnitActionType.Explode(5, 3));
    }
  }

  // sea

  public static class BBoatModel extends AWBWUnitModel
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
      super("BBoat", AWBWUnitEnum.BBOAT, UnitRoleEnum.TRANSPORT, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
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

  public static class LanderModel extends AWBWUnitModel
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
      super("Lander", AWBWUnitEnum.LANDER, UnitRoleEnum.TRANSPORT, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
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

  public static class SubModel extends AWBWUnitModel
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
      super("Sub", AWBWUnitEnum.SUB, UnitRoleEnum.ASSAULT, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER, moveType,
          actions, weapons);
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
      chassis = ChassisEnum.SUBMERGED;
      idleFuelBurn = IDLE_FUEL_BURN;
      hidden = true;
    }
  }

  public static class BattleshipModel extends AWBWUnitModel
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
      super("Battleship", AWBWUnitEnum.BATTLESHIP, UnitRoleEnum.SIEGE, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons);
    }
  }

  public static class CarrierModel extends AWBWUnitModel
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

    public CarrierModel() // TODO: Resupply held units
    {
      super("Carrier", AWBWUnitEnum.CARRIER, UnitRoleEnum.TRANSPORT, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
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

  public static class CruiserModel extends AWBWUnitModel
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
      super("Cruiser", AWBWUnitEnum.CRUISER, UnitRoleEnum.ANTI_AIR, ChassisEnum.SHIP, UNIT_COST, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
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
