package Units;

import java.util.ArrayList;
import java.util.Arrays;

import Engine.UnitActionFactory;
import Engine.UnitActionLifecycles.FlareLifecycle;
import Engine.UnitActionLifecycles.TerraformLifecycle;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Engine.UnitActionLifecycles.UnitProduceLifecycle;
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
import lombok.var;
import lombok.experimental.SuperBuilder;

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
    factoryModels.add(InfantryModel());
    factoryModels.add(MechModel());
    factoryModels.add(BikeModel());
    factoryModels.add(ReconModel());
    factoryModels.add(FlareModel());
    factoryModels.add(AntiAirModel());
    factoryModels.add(TankModel());
    factoryModels.add(MDTankModel());
    factoryModels.add(WarTankModel());
    factoryModels.add(ArtilleryModel());
    factoryModels.add(AntiTankModel());
    factoryModels.add(RocketsModel());
    factoryModels.add(MobileSAMModel());
    factoryModels.add(RigModel());

    // Record those units we can get from a Seaport.
    seaportModels.add(GunboatModel());
    seaportModels.add(CruiserModel());
    UnitModel subsub  = SubSubModel(); // Subs are built submerged
    seaportModels.add(subsub);
    UnitModel carrier = CarrierModel();
    seaportModels.add(carrier);
    seaportModels.add(BattleshipModel());
    seaportModels.add(LanderModel());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(FighterModel());
    airportModels.add(BomberModel());
    airportModels.add(DusterModel());
    airportModels.add(BCopterModel());
    airportModels.add(TCopterModel());

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
    UnitModel sub = SubModel();
    sub   .baseActions.add(new TransformLifecycle.TransformFactory(subsub, "DIVE"));
    subsub.baseActions.add(new TransformLifecycle.TransformFactory(sub, "RISE"));
    dorModels.unitModels.add(sub);

    UnitModel seaplane = SeaplaneModel();
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

  @SuperBuilder(toBuilder = true)
  public static class DoRUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public DoRUnitEnum type;

    @Override
    public int getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }
  }

  public DoRUnitModel InfantryModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TROOP | UnitModel.LAND);
    
    b.costBase(1500);
    b.abilityPowerValue(4);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(2);
    b.baseMovePower(3);

    b.baseMoveType(new FootStandard());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.InfantryMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Infantry");
    b.type(DoRUnitEnum.INFANTRY);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel MechModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TROOP | UnitModel.MECH | UnitModel.LAND);
    
    b.costBase(2500);
    b.abilityPowerValue(4);
    b.maxFuel(70);
    b.fuelBurnIdle(0);
    b.maxAmmo(3);
    b.visionRange(2);
    b.baseMovePower(2);

    b.baseMoveType(new FootMech());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.MechZooka(), new DoRWeapons.MechMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Mech");
    b.type(DoRUnitEnum.MECH);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel BikeModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TROOP | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(2500);
    b.abilityPowerValue(4);
    b.maxFuel(70);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(2);
    b.baseMovePower(5);

    b.baseMoveType(new TiresRugged());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.MechMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Bike");
    b.type(DoRUnitEnum.BIKE);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel ReconModel()
  {
    var b = DoRUnitModel.builder();
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
    WeaponModel[] weapons = { new DoRWeapons.ReconMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Recon");
    b.type(DoRUnitEnum.RECON);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel FlareModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.RECON | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(5000);
    b.abilityPowerValue(10);
    b.maxFuel(60);
    b.fuelBurnIdle(0);
    b.maxAmmo(3);
    b.visionRange(2);
    b.baseMovePower(5);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.FlareMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Flare");
    b.type(DoRUnitEnum.FLARE);
    DoRUnitModel output = b.build();
    output.baseActions.add(0, new FlareLifecycle.FlareFactory(0, 5, 2));
    return output;
  }

  public DoRUnitModel AntiAirModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(7000);
    b.abilityPowerValue(10);
    b.maxFuel(60);
    b.fuelBurnIdle(0);
    b.maxAmmo(6);
    b.visionRange(3);
    b.baseMovePower(6);
    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.AntiAirMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Anti-Air");
    b.type(DoRUnitEnum.ANTI_AIR);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel TankModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(7000);
    b.abilityPowerValue(10);
    b.maxFuel(70);
    b.fuelBurnIdle(0);
    b.maxAmmo(6);
    b.visionRange(3);
    b.baseMovePower(6);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.TankCannon(), new DoRWeapons.TankMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Tank");
    b.type(DoRUnitEnum.TANK);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel MDTankModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(12000);
    b.abilityPowerValue(14);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(5);
    b.visionRange(2);
    b.baseMovePower(5);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.MDTankCannon(), new DoRWeapons.MDTankMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Md Tank");
    b.type(DoRUnitEnum.MD_TANK);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel WarTankModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(16000);
    b.abilityPowerValue(16);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(5);
    b.visionRange(2);
    b.baseMovePower(4);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.WarCannon(), new DoRWeapons.WarMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("War Tank");
    b.type(DoRUnitEnum.WAR_TANK);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel ArtilleryModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(6000);
    b.abilityPowerValue(10);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(6);
    b.visionRange(3);
    b.baseMovePower(5);

    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.ArtilleryCannon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Artillery");
    b.type(DoRUnitEnum.ARTILLERY);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel AntiTankModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(11000);
    b.abilityPowerValue(14);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(6);
    b.visionRange(2);
    b.baseMovePower(4);

    b.baseMoveType(new TiresRugged());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.AntiTankCannon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("AntiTank");
    b.type(DoRUnitEnum.ANTITANK);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel RocketsModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(15000);
    b.abilityPowerValue(14);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(5);
    b.visionRange(3);
    b.baseMovePower(5);

    b.baseMoveType(new Tires());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.RocketRockets() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Rockets");
    b.type(DoRUnitEnum.ROCKETS);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel MobileSAMModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(12000);
    b.abilityPowerValue(14);
    b.maxFuel(50);
    b.fuelBurnIdle(0);
    b.maxAmmo(5);
    b.visionRange(5);
    b.baseMovePower(5); // Finally, sigh

    b.baseMoveType(new Tires());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.MobileSAMWeapon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Missiles");
    b.type(DoRUnitEnum.MOBILESAM);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel RigModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.TANK | UnitModel.LAND);
    
    b.costBase(5000);
    b.abilityPowerValue(8);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(6);
    b.baseMoveType(new Tread());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.APC_ACTIONS)));

    b.name("APC");
    b.type(DoRUnitEnum.RIG);
    b.baseCargoCapacity(1);
    b.carryableMask(UnitModel.TROOP);
    b.carryableExclusionMask(UnitModel.TANK); // Can't carry Bikes

    b.maxMaterials(1);
    DoRUnitModel output = b.build();
          output.baseActions.add(
          new TerraformLifecycle.TerraformFactory(TerrainType.GRASS, TerrainType.TEMP_AIRPORT, "BUILD"));
          output.baseActions.add(
          new TerraformLifecycle.TerraformFactory(TerrainType.SHOAL, TerrainType.TEMP_SEAPORT, "BUILD"));
    return output;
  }

  // air

  public DoRUnitModel FighterModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.AIR_TO_AIR | UnitModel.JET | UnitModel.AIR);
    
    b.costBase(20000);
    b.abilityPowerValue(18);
    b.maxFuel(99);
    b.fuelBurnIdle(5);
    b.maxAmmo(6);
    b.visionRange(5);
    b.baseMovePower(9);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.FighterMissiles() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Fighter");
    b.type(DoRUnitEnum.FIGHTER);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel BomberModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT | UnitModel.JET | UnitModel.AIR);
    
    b.costBase(20000);
    b.abilityPowerValue(18);
    b.maxFuel(99);
    b.fuelBurnIdle(5);
    b.maxAmmo(6);
    b.visionRange(3);
    b.baseMovePower(7);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.BomberBombs() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Bomber");
    b.type(DoRUnitEnum.BOMBER);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel SeaplaneModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.ASSAULT | UnitModel.JET | UnitModel.AIR);
    
    b.costBase(15000);
    b.abilityPowerValue(14);
    b.maxFuel(40);
    b.fuelBurnIdle(5);
    b.maxAmmo(3);
    b.visionRange(4);
    b.baseMovePower(7);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.SeaplaneShots() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Seaplane");
    b.type(DoRUnitEnum.SEAPLANE);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel DusterModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.AIR_TO_AIR | UnitModel.JET | UnitModel.AIR);
    
    b.costBase(13000);
    b.abilityPowerValue(14);
    b.maxFuel(99);
    b.fuelBurnIdle(5);
    b.maxAmmo(9);
    b.visionRange(4);
    b.baseMovePower(8);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.DusterMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Duster");
    b.type(DoRUnitEnum.DUSTER);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel BCopterModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT | UnitModel.HOVER | UnitModel.AIR);
    
    b.costBase(9000);
    b.abilityPowerValue(12);
    b.maxFuel(99);
    b.fuelBurnIdle(2);
    b.maxAmmo(6);
    b.visionRange(2);
    b.baseMovePower(6);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.CopterRockets(), new DoRWeapons.CopterMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("B-Copter");
    b.type(DoRUnitEnum.B_COPTER);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel TCopterModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.HOVER | UnitModel.AIR);
    
    b.costBase(5000);
    b.abilityPowerValue(10);
    b.maxFuel(99);
    b.fuelBurnIdle(2);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(6);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    b.name("T-Copter");
    b.type(DoRUnitEnum.T_COPTER);
    b.baseCargoCapacity(1);
    b.carryableMask(UnitModel.TROOP);
    b.carryableExclusionMask(UnitModel.TANK); // Can't carry Bikes
    DoRUnitModel output = b.build();
    return output;
  }

  // sea

  public DoRUnitModel GunboatModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(6000);
    b.abilityPowerValue(10);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(1);
    b.visionRange(2);
    b.baseMovePower(7);

    b.baseMoveType(new FloatLight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_TRANSPORT_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.GunBoatGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Gunboat");
    b.type(DoRUnitEnum.GUNBOAT);
    b.baseCargoCapacity(1);
    b.carryableMask(UnitModel.TROOP);
    b.carryableExclusionMask(UnitModel.TANK); // Can't carry Bikes
    DoRUnitModel output = b.build();
    output.unloadExclusionTerrain.add(TerrainType.BRIDGE);
    return output;
  }

  public DoRUnitModel CruiserModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SURFACE_TO_AIR | UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(16000);
    b.abilityPowerValue(16);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(9);
    b.visionRange(5);
    b.baseMovePower(6);

    b.baseMoveType(DoRFloatHeavy);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_TRANSPORT_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.CruiserTorpedoes(), new DoRWeapons.CruiserMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Cruiser");
    b.type(DoRUnitEnum.CRUISER);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.AIR);

      /** Cruisers supply their cargo at the beginning of every turn. Make it so. */
    b.supplyCargo(true);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel SubModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(20000);
    b.abilityPowerValue(18);
    b.maxFuel(70);
    b.fuelBurnIdle(1);
    b.maxAmmo(6);
    b.visionRange(5);
    b.baseMovePower(6);

    b.baseMoveType(DoRFloatHeavy);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.SubTorpedoes() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Sub");
    b.type(DoRUnitEnum.SUB);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel SubSubModel()
  {
    var b = SubModel().toBuilder();
    b.fuelBurnIdle(5);
    b.type(DoRUnitEnum.SUB_SUB);
    b.hidden(true);
    DoRUnitModel output = b.build();
    output.role |= UnitModel.SUBSURFACE;
    return output;
  }

  public DoRUnitModel CarrierModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SURFACE_TO_AIR | UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(28000);
    b.abilityPowerValue(22);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(-1);
    b.visionRange(4);
    b.baseMovePower(5);

    b.baseMoveType(DoRFloatHeavy);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.CarrierMGun() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Carrier");
    b.type(DoRUnitEnum.CARRIER);
    b.maxMaterials(4);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.AIR);
    /** DoR Carriers repair and supply their cargo at the beginning of every turn. Make it so. */
    b.repairCargo(true);
    b.supplyCargo(true);
    DoRUnitModel output = b.build();
    output.baseActions.add(0, UnitActionFactory.LAUNCH);
    return output;
  }

  public DoRUnitModel BattleshipModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.SIEGE | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(25000);
    b.abilityPowerValue(20);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(6);
    b.visionRange(3);
    b.baseMovePower(5);

    b.baseMoveType(DoRFloatHeavy);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new DoRWeapons.BattleshipCannon() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Battleship");
    b.type(DoRUnitEnum.BATTLESHIP);
    DoRUnitModel output = b.build();
    return output;
  }

  public DoRUnitModel LanderModel()
  {
    var b = DoRUnitModel.builder();
    b.role(UnitModel.TRANSPORT | UnitModel.SHIP | UnitModel.SEA);
    
    b.costBase(10000);
    b.abilityPowerValue(12);
    b.maxFuel(99);
    b.fuelBurnIdle(1);
    b.maxAmmo(-1);
    b.visionRange(1);
    b.baseMovePower(6);

    b.baseMoveType(new FloatLight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    b.name("Lander");
    b.type(DoRUnitEnum.LANDER);
    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.TROOP | UnitModel.TANK);
    DoRUnitModel output = b.build();
    output.unloadExclusionTerrain.add(TerrainType.BRIDGE);
    return output;
  }

}
