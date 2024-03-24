package Units;

import java.util.ArrayList;
import java.util.Arrays;

import Engine.UnitActionFactory;
import Engine.UnitActionLifecycles.ExplodeLifecycle;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Terrain.TerrainType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.FloatLight;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.MoveTypes.Tread;
import lombok.var;
import lombok.experimental.SuperBuilder;

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
    factoryModels.add(InfantryModel());
    factoryModels.add(MechModel());
    factoryModels.add(APCModel());
    factoryModels.add(ArtilleryModel());
    factoryModels.add(ReconModel());
    factoryModels.add(TankModel());
    factoryModels.add(MDTankModel());
    factoryModels.add(NeotankModel());
    factoryModels.add(MegatankModel());
    factoryModels.add(RocketsModel());
    factoryModels.add(AntiAirModel());
    factoryModels.add(MobileSAMModel());
    factoryModels.add(PiperunnerModel());

    // Record those units we can get from a Seaport.
    seaportModels.add(LanderModel());
    seaportModels.add(CruiserModel());
    UnitModel sub = SubModel();
    seaportModels.add(sub);
    seaportModels.add(BattleshipModel());
    seaportModels.add(CarrierModel());
    seaportModels.add(BBoatModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(TCopterModel());
    airportModels.add(BCopterModel());
    airportModels.add(FighterModel());
    airportModels.add(BomberModel());
    UnitModel stealth = StealthModel();
    airportModels.add(stealth);
    airportModels.add(BBombModel());

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
    UnitModel subsub = SubSubModel();
    sub   .baseActions.add(new TransformLifecycle.TransformFactory(subsub, "DIVE"));
    subsub.baseActions.add(new TransformLifecycle.TransformFactory(sub, "RISE"));
    awbwModels.unitModels.add(subsub);
    UnitModel sneaky = StealthHideModel();
    stealth.baseActions.add(new TransformLifecycle.TransformFactory(sneaky, "HIDE"));
    sneaky .baseActions.add(new TransformLifecycle.TransformFactory(stealth, "APPEAR"));
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

  @SuperBuilder(toBuilder = true)
  public static class AWBWUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public AWBWUnitEnum type;

    @Override
    public int getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }
  }

  public AWBWUnitModel InfantryModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.TROOP | UnitModel.LAND);
    
    b.costBase(1000);
    b.abilityPowerValue(4);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(2);
    b.baseMovePower(3);

    b.baseMoveType(new FootStandard());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.InfantryMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Infantry");
    b.type(AWBWUnitEnum.INFANTRY);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel MechModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.MECH | UnitModel.TROOP | UnitModel.LAND);
    
    b.costBase(3000);
    b.abilityPowerValue(4);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(3);
    b.visionRange(2);
    b.baseMovePower(2);

    b.baseMoveType(new FootMech());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.MechZooka(), new AWBWWeapons.MechMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Mech");
    b.type(AWBWUnitEnum.MECH);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel APCModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(5000);
    b.abilityPowerValue(8);
    b.maxFuel(70);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(6);
    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.APC_ACTIONS)));

    b.name("APC");
    b.type(AWBWUnitEnum.APC);
    b.baseCargoCapacity(1);
    b.carryableMask(UnitModel.TROOP);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel ReconModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.RECON | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(4000);
    b.abilityPowerValue(10);
    b.maxFuel(80);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(5);
    b.baseMovePower(8);

    b.baseMoveType(new Tires());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.ReconMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Recon");
    b.type(AWBWUnitEnum.RECON);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel TankModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(7000);
    b.abilityPowerValue(10);
    b.maxFuel(70);
    b.fuelBurnIdle(0);
    b.maxAmmo(9);
    b.visionRange(3);
    b.baseMovePower(6);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.TankCannon(), new AWBWWeapons.TankMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Tank");
    b.type(AWBWUnitEnum.TANK);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel MDTankModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(16000);
    b.abilityPowerValue(16);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(8);
    b.visionRange(1);
    b.baseMovePower(5);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.MDTankCannon(), new AWBWWeapons.MDTankMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Md Tank");
    b.type(AWBWUnitEnum.MD_TANK);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel NeotankModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(22000);
    b.abilityPowerValue(18);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(9);
    b.visionRange(1);
    b.baseMovePower(6);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.NeoCannon(), new AWBWWeapons.NeoMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Neotank");
    b.type(AWBWUnitEnum.NEOTANK);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel MegatankModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(28000);
    b.abilityPowerValue(22);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(3);
    b.visionRange(1);
    b.baseMovePower(4);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.MegaCannon(), new AWBWWeapons.MegaMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Megatank");
    b.type(AWBWUnitEnum.MEGATANK);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel ArtilleryModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(6000);
    b.abilityPowerValue(10);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(9);
    b.visionRange(1);
    b.baseMovePower(5);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.ArtilleryCannon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Artillery");
    b.type(AWBWUnitEnum.ARTILLERY);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel RocketsModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(15000);
    b.abilityPowerValue(14);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(6);
    b.visionRange(1);
    b.baseMovePower(5);

    b.baseMoveType(new Tires());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.RocketRockets() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Rockets");
    b.type(AWBWUnitEnum.ROCKETS);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel PiperunnerModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(20000);
    b.abilityPowerValue(20);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(9);
    b.visionRange(4);
    b.baseMovePower(9);

    b.baseMoveType(new MoveType());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.PipeGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Piperunner");
    b.type(AWBWUnitEnum.PIPERUNNER);
    AWBWUnitModel output = b.build();
    output.baseMoveType.setMoveCost(TerrainType.PILLAR, 1);
    output.baseMoveType.setMoveCost(TerrainType.METEOR, 1);
    output.baseMoveType.setMoveCost(TerrainType.FACTORY, 1);
    return output;
  }

  public AWBWUnitModel AntiAirModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(8000);
    b.abilityPowerValue(10);
    b.maxFuel(60);
    b.fuelBurnIdle(0);
    b.maxAmmo(9);
    b.visionRange(2);
    b.baseMovePower(6);
    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.AntiAirMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Anti-Air");
    b.type(AWBWUnitEnum.ANTI_AIR);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel MobileSAMModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(12000);
    b.abilityPowerValue(14);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(9);
    b.visionRange(5);
    b.baseMovePower(4);

    b.baseMoveType(new Tires());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.MobileSAMWeapon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Missiles");
    b.type(AWBWUnitEnum.MOBILESAM);
    AWBWUnitModel output = b.build();
    return output;
  }

  // air

  public AWBWUnitModel TCopterModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.HOVER | UnitModel.AIR_LOW);
    
    b.costBase(5000);
    b.abilityPowerValue(10);
    b.maxFuel(99);
    b.fuelBurnIdle(2);
    b.maxAmmo(-1);
    b.visionRange(2);
    b.baseMovePower(6);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    b.name("T-Copter");
    b.type(AWBWUnitEnum.T_COPTER);
    b.baseCargoCapacity(1);
    b.carryableMask(UnitModel.TROOP);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel BCopterModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT | UnitModel.HOVER | UnitModel.AIR_LOW);
    
    b.costBase(9000);
    b.abilityPowerValue(12);
    b.maxFuel(99);
    b.fuelBurnIdle(2);
    b.maxAmmo(6);
    b.visionRange(3);
    b.baseMovePower(6);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.CopterRockets(), new AWBWWeapons.CopterMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("B-Copter");
    b.type(AWBWUnitEnum.B_COPTER);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel BomberModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT | UnitModel.JET | UnitModel.AIR_HIGH);
    
    b.costBase(22000);
    b.abilityPowerValue(18);
    b.maxFuel(99);
    b.fuelBurnIdle(5);
    b.maxAmmo(9);
    b.visionRange(2);
    b.baseMovePower(7);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.BomberBombs() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Bomber");
    b.type(AWBWUnitEnum.BOMBER);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel FighterModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.AIR_TO_AIR | UnitModel.JET | UnitModel.AIR_HIGH);
    
    b.costBase(20000);
    b.abilityPowerValue(18);
    b.maxFuel(99);
    b.fuelBurnIdle(5);
    b.maxAmmo(9);
    b.visionRange(2);
    b.baseMovePower(9);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.FighterMissiles() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Fighter");
    b.type(AWBWUnitEnum.FIGHTER);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel StealthModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.ASSAULT | UnitModel.JET | UnitModel.AIR_HIGH);
    
    b.costBase(24000);
    b.abilityPowerValue(20);
    b.maxFuel(60);
    b.fuelBurnIdle(5);
    b.maxAmmo(6);
    b.visionRange(4);
    b.baseMovePower(6);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.StealthShots() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Stealth");
    b.type(AWBWUnitEnum.STEALTH);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel StealthHideModel()
  {
    var b = StealthModel().toBuilder();
    b.fuelBurnIdle(8);

    b.type(AWBWUnitEnum.STEALTH_HIDE);
    b.hidden(true);
    AWBWUnitModel output = b.build();
    return output;
  }

  private static final int EXPLODE_RADIUS = 3;
  private static final int EXPLODE_POWER = 50;
  public AWBWUnitModel BBombModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.JET | UnitModel.AIR_HIGH);
    
    b.costBase(25000);
    b.abilityPowerValue(6);
    b.maxFuel(45);
    b.fuelBurnIdle(5);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(9);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.BASIC_ACTIONS)));

    b.name("BBomb");
    b.type(AWBWUnitEnum.BBOMB);
    AWBWUnitModel output = b.build();
    output.baseActions.add(0, new ExplodeLifecycle.ExplodeFactory(EXPLODE_POWER, EXPLODE_RADIUS));
    return output;
  }

  // sea

  public AWBWUnitModel BBoatModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(7500);
    b.abilityPowerValue(10);
    b.maxFuel(60);
    b.fuelBurnIdle(1);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(7);

    b.baseMoveType(new FloatLight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    b.name("BBoat");
    b.type(AWBWUnitEnum.BBOAT);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.TROOP);
    AWBWUnitModel output = b.build();
    output.baseActions.add(0, UnitActionFactory.REPAIR_UNIT);
    return output;
  }

  public AWBWUnitModel LanderModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);

    b.costBase(12000);
    b.abilityPowerValue(12);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(6);

    b.baseMoveType(new FloatLight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    b.name("Lander");
    b.type(AWBWUnitEnum.LANDER);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.TROOP | UnitModel.TANK);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel SubModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SHIP | UnitModel.SEA);

    b.costBase(20000);
    b.abilityPowerValue(18);
    b.maxFuel(60);
    b.fuelBurnIdle(1);
    b.maxAmmo(6);
    b.visionRange(5);
    b.baseMovePower(5);

    b.baseMoveType(new FloatHeavy());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.SubTorpedoes() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Sub");
    b.type(AWBWUnitEnum.SUB);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel SubSubModel()
  {
    var b = SubModel().toBuilder();
    b.fuelBurnIdle(5);

    b.type(AWBWUnitEnum.SUB_SUB);
    b.hidden(true);
    AWBWUnitModel output = b.build();
    output.role |= UnitModel.SUBSURFACE;
    return output;
  }

  public AWBWUnitModel BattleshipModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(28000);
    b.abilityPowerValue(22);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(9);
    b.visionRange(2);
    b.baseMovePower(5);

    b.baseMoveType(new FloatHeavy());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.BattleshipCannon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Battleship");
    b.type(AWBWUnitEnum.BATTLESHIP);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel CarrierModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.SURFACE_TO_AIR | UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(30000);
    b.abilityPowerValue(22);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(9);
    b.visionRange(4);
    b.baseMovePower(5);

    b.baseMoveType(new FloatHeavy());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_TRANSPORT_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.CarrierMissiles() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Carrier");
    b.type(AWBWUnitEnum.CARRIER);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.AIR_LOW | UnitModel.AIR_HIGH);

    /** Carriers supply their cargo at the beginning of every turn. Make it so. */
    b.supplyCargo(true);
    AWBWUnitModel output = b.build();
    return output;
  }

  public AWBWUnitModel CruiserModel()
  {
    var b = AWBWUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.SURFACE_TO_AIR | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(18000);
    b.abilityPowerValue(16);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(9);
    b.visionRange(3);
    b.baseMovePower(6);

    b.baseMoveType(new FloatHeavy());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_TRANSPORT_ACTIONS)));
    WeaponModel[] weapons = { new AWBWWeapons.CruiserTorpedoes(), new AWBWWeapons.CruiserMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Cruiser");
    b.type(AWBWUnitEnum.CRUISER);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.AIR_LOW);

    /** Cruisers supply their cargo at the beginning of every turn. Make it so. */
    b.supplyCargo(true);
    AWBWUnitModel output = b.build();
    return output;
  }

}
