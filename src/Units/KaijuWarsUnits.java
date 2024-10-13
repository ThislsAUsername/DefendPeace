package Units;

import java.util.ArrayList;
import java.util.Arrays;

import Engine.GameInstance;
import Engine.UnitActionFactory;
import Engine.StateTrackers.StateTracker;
import Engine.StateTrackers.SuicideAttackTracker;
import Engine.StateTrackers.UnitResurrectionTracker;
import Engine.StateTrackers.UnitTurnPositionTracker;
import Engine.UnitActionLifecycles.UnitProduceLifecycle;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.KaijuWarsUnits.KaijuWarsUnitModel.KaijuWarsUnitModelBuilder;
import Units.KaijuWarsWeapons.CombatStunApplier;
import Units.MoveTypes.Flight;
import Units.MoveTypes.FootMech;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import lombok.Builder;
import lombok.var;
import lombok.experimental.SuperBuilder;

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
    factoryModels.add(Police());
    factoryModels.add(Infantry());
    factoryModels.add(Tank());
    factoryModels.add(Missiles());
    factoryModels.add(Rockets());
    factoryModels.add(AA());
    factoryModels.add(Radar());
    factoryModels.add(Artillery());
    factoryModels.add(Maser());
    factoryModels.add(LigerPanther());
    factoryModels.add(OGRPlatform());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(Fighter());
    airportModels.add(Bomber());
    airportModels.add(Helicopter());
    airportModels.add(Bushplane());

    // Carrier gets to build all normal air units
    UnitModel carrier = SkyCarrier();
    // Iterate backwards so the insertion ends up in the right order
    for( int i = airportModels.size() - 1; i >= 0; --i )
      carrier.baseActions.add(1, new UnitProduceLifecycle.UnitProduceFactory(airportModels.get(i)));

    airportModels.add(BigBoy());
    KaijuWarsUnitModel z2Hurt = SuperZ2Hurt();
    airportModels.add(SuperZ2(z2Hurt));
    extras.add(z2Hurt);
    KaijuWarsUnitModel gunBot = GuncrossRobot();
    airportModels.add(GuncrossWing(gunBot));
    airportModels.add(Kaputnik());
    extras.add(gunBot);
    airportModels.add(carrier);

    // Throw in some "bonus units" to show the damage to entrenched inf/fighters
    KaijuWarsUnitModel entrenched = Infantry().toBuilder().hidden(true).build();
    entrenched.kaijuCounter += KaijuWarsWeapons.STATIC_INF_BONUS;
    KaijuWarsUnitModel divineWind = Fighter().toBuilder().hidden(true).build();
    divineWind.kaijuCounter += KaijuWarsWeapons.DIVINE_WIND_BONUS;
    extras.add(entrenched);
    extras.add(divineWind);

    // Record those units we can get from a Seaport.
    kaijuModels.add(KaijuWarsKaiju.Alphazaurus());
    final KaijuWarsKaiju.HellTurkey     turkeyAir  = KaijuWarsKaiju.HellTurkey();
    final KaijuWarsKaiju.HellTurkeyLand turkeyLand = KaijuWarsKaiju.HellTurkeyLand(turkeyAir);
    final KaijuWarsKaiju.HellTurkeyEgg  turkeyEgg  = KaijuWarsKaiju.HellTurkeyEgg(turkeyLand);
    turkeyAir.turkeyLand = turkeyLand;
    turkeyAir.turkeyEgg = turkeyEgg;
    kaijuModels.add(turkeyAir);
    extras.add(turkeyLand);
    extras.add(turkeyEgg);
    final KaijuWarsKaiju.BigDonk rampager = KaijuWarsKaiju.BigDonkRampage();
    extras.add(rampager);
    kaijuModels.add(KaijuWarsKaiju.BigDonk(rampager));
    final KaijuWarsKaiju.Snek tunneler = KaijuWarsKaiju.SnekTunneler();
    extras.add(tunneler);
    kaijuModels.add(KaijuWarsKaiju.Snek(tunneler));
    final KaijuWarsKaiju.UFO abductor = KaijuWarsKaiju.UFOAbducts();
    extras.add(abductor);
    kaijuModels.add(KaijuWarsKaiju.UFO(abductor));

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

    // Make sure the combat modifier's on there
    var fightMod = new KaijuWarsWeapons.KaijuWarsFightMod();
    for( UnitModel um : kjwModels.unitModels )
      um.addUnitModifier(fightMod);

    return kjwModels;
  }

  public void registerStateTrackers(GameInstance gi)
  {
    super.registerStateTrackers(gi);

    StateTracker.instance(gi, UnitTurnPositionTracker.class);
    StateTracker.instance(gi, CombatStunApplier.class);

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

  @SuperBuilder(toBuilder = true)
  public static class KaijuWarsUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    @Builder.Default public int kaijuCounter = 0;
    @Builder.Default public boolean entrenches     = false;
    @Builder.Default public boolean stillBoost     = false;
    @Builder.Default public boolean divineWind     = false; // +2 counter on plains/sea
    @Builder.Default public boolean boostsAllies   = false;
    @Builder.Default public boolean boostSurround  = false;

    @Builder.Default public UnitModel resurrectsAs = null;
    @Builder.Default public boolean suicideAttack  = false;

    @Builder.Default public boolean slowsLand      = false;
    @Builder.Default public boolean slowsAir       = false;
    @Builder.Default public boolean resistsKaiju   = false;
    @Builder.Default public boolean isKaiju        = false;

    @Override
    public int getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }
  }

  public static final int UNIT_COST = 3000;
  // These go for all units that aren't experiments
  private static KaijuWarsUnitModelBuilder<?, ?> baseBuilder()
  {
    return baseBuilder(null);
  }
  private static KaijuWarsUnitModelBuilder<?, ?> baseBuilder(KaijuWarsUnitModelBuilder<?, ?> b)
  {
    if( null == b )
      b = KaijuWarsUnitModel.builder();
    b.costBase(UNIT_COST);
    b.abilityPowerValue(10);
    b.maxFuel(99);
    b.fuelBurnIdle(0);
    b.maxAmmo(-1);
    b.visionRange(2);
    b.fuelBurnPerTile(0);
    b.needsMaterials(false);
    return b;
  }
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

  public KaijuWarsUnitModel Police()
  {
    var b = baseBuilder();
    b.role(UnitModel.TROOP | UnitModel.LAND);

    b.baseMovePower(3);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(1) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Police");
    b.kaijuCounter(1);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Infantry()
  {
    var b = baseBuilder();
    b.role(UnitModel.TROOP | UnitModel.LAND);

    b.baseMovePower(3);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(1) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Infantry");
    b.kaijuCounter(1);
    b.entrenches(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Tank()
  {
    var b = baseBuilder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(1) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Tank");
    b.kaijuCounter(2);
    b.slowsLand(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Missiles()
  {
    var b = baseBuilder();
    // Mech because they're relatively slow and relatively strong
    b.role(UnitModel.MECH | UnitModel.SIEGE | UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);

    b.baseMoveType(AWKWARD);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.Missile() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Missiles");
    b.kaijuCounter(0);
    b.stillBoost(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }
  public KaijuWarsUnitModel Rockets()
  {
    var b = Missiles().toBuilder();
    b.name("Rockets");
    b.costBase(UNIT_COST * 3);
    KaijuWarsUnitModel output = b.build();
    output.weapons.clear();
    output.weapons.add(new KaijuWarsWeapons.Rockets());
    return output;
  }

  public KaijuWarsUnitModel AA()
  {
    var b = baseBuilder();
    b.role(UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.AA() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Anti-Air");
    b.kaijuCounter(1);
    b.slowsAir(true);
    b.boostSurround(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  // Separate class so it can be instanceof'd
  @SuperBuilder(toBuilder = true)
  public static class Radar extends KaijuWarsUnitModel
  {
    private static final long serialVersionUID = 1L;
    public static final int VISION_RANGE = 2;
  }
  public KaijuWarsUnitModel Radar()
  {
    var b = baseBuilder(Radar.builder());
    b.role(UnitModel.RECON | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);
    b.visionRange(Radar.VISION_RANGE);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.ShootAll(1) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Radar");
    b.kaijuCounter(0);
    // For spotting tunneling kaiju
    b.visionPierces(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Artillery()
  {
    var b = baseBuilder();
    b.role(UnitModel.SIEGE | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.Artillery() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Artillery");
    b.kaijuCounter(0);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  // air

  public KaijuWarsUnitModel Fighter()
  {
    var b = baseBuilder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.JET | UnitModel.AIR);

    b.baseMovePower(4);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.Fighter() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Fighter");
    b.kaijuCounter(2);
    b.divineWind(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Bomber()
  {
    var b = baseBuilder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT | UnitModel.JET | UnitModel.AIR);

    b.baseMovePower(3);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.Bomber() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Bomber");
    b.kaijuCounter(1);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Helicopter()
  {
    var b = baseBuilder();
    b.role(UnitModel.HOVER | UnitModel.AIR);

    b.baseMovePower(2);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.BASIC_ACTIONS)));

    b.name("Helicopter");
    b.kaijuCounter(1);
    b.boostsAllies(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel Bushplane()
  {
    var b = baseBuilder();
    b.role(UnitModel.JET | UnitModel.AIR);

    b.baseMovePower(4);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.BASIC_ACTIONS)));

    b.name("Bushplane");
    b.kaijuCounter(0);
    KaijuWarsUnitModel output = b.build();
    output.baseActions.add(0, UnitActionFactory.CAPTURE);
    return output;
  }

  // Experimental weapons

  // Cost per turn of prep the experimental weapon normally takes
  private static final int PREP_COST = 3000;

  public KaijuWarsUnitModel Maser()
  {
    var b = baseBuilder();
    b.role(UnitModel.MECH | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);
    b.costBase(PREP_COST * 4);

    b.baseMoveType(AWKWARD);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.Maser() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Maser");
    b.kaijuCounter(0);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel BigBoy()
  {
    var b = baseBuilder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT | UnitModel.JET | UnitModel.AIR);

    b.baseMovePower(3);
    b.costBase(PREP_COST * 3);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.BigBoy() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Big Boy");
    b.kaijuCounter(2);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel SkyCarrier()
  {
    var b = baseBuilder();
    b.role(UnitModel.HOVER | UnitModel.AIR | UnitModel.TRANSPORT);

    b.baseMovePower(3);
    b.costBase(PREP_COST * 7); // Consider reducing this since this version is nerfed

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.BASIC_ACTIONS)));

    b.name("Sky Carrier");
    b.baseCargoCapacity(4);
    b.carryableMask(UnitModel.AIR);
      // Let's allow stacking carriers, because why not?
    b.carryableExclusionMask(UnitModel.TRANSPORT);

    b.kaijuCounter(5);
    b.boostsAllies(true);

      /** Repair cargo to (badly) simulate repairing lots of dudes in the source game */
    b.repairCargo(true);
    b.supplyCargo(true);
    KaijuWarsUnitModel output = b.build();
    output.baseActions.add(0, UnitActionFactory.LAUNCH);
    return output;
  }

  public KaijuWarsUnitModel Kaputnik()
  {
    var b = baseBuilder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.JET | UnitModel.AIR);

    b.baseMovePower(8);
    b.costBase(PREP_COST * 5);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.Kaputnik() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Kaputnik");
    b.kaijuCounter(0);
    b.suicideAttack(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel LigerPanther()
  {
    var b = baseBuilder();
    b.role(UnitModel.ASSAULT | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(3);
    b.costBase(PREP_COST * 2);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.ShootLand(3) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Liger Panther");
    b.kaijuCounter(3);
    b.slowsLand(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel SuperZ2(UnitModel rezTo)
  {
    var b = baseBuilder();
    b.role(UnitModel.ASSAULT | UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.TANK | UnitModel.HOVER | UnitModel.AIR);

    b.baseMovePower(3);
    b.costBase(PREP_COST * 4);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.SuperZ2() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Super Z2");
    b.kaijuCounter(4);
    b.resurrectsAs(rezTo);
    KaijuWarsUnitModel output = b.build();
    return output;
  }
  public KaijuWarsUnitModel SuperZ2Hurt()
  {
    var b = SuperZ2(null).toBuilder();

    b.costBase(PREP_COST * 1);
    b.name("Super Z2 hurt");
    b.kaijuCounter(1);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel GuncrossWing(UnitModel rezTo)
  {
    var b = baseBuilder();
    b.role(UnitModel.AIR_TO_SURFACE | UnitModel.AIR_TO_AIR | UnitModel.JET | UnitModel.AIR);

    b.baseMovePower(5);
    b.costBase(PREP_COST * 5);

    b.baseMoveType(new Flight());
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.GuncrossWing() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Guncross Wing");
    b.kaijuCounter(3);
    b.resurrectsAs(rezTo);
    KaijuWarsUnitModel output = b.build();
    return output;
  }
  public KaijuWarsUnitModel GuncrossRobot()
  {
    var b = baseBuilder();
    b.role(UnitModel.TANK | UnitModel.ASSAULT | UnitModel.LAND);

    b.baseMovePower(3);
    b.costBase(PREP_COST * 2);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.GuncrossRobot() };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Guncross Robot");
    b.kaijuCounter(2);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

  public KaijuWarsUnitModel OGRPlatform()
  {
    var b = baseBuilder();
    b.role(UnitModel.ASSAULT | UnitModel.SURFACE_TO_AIR | UnitModel.TANK | UnitModel.LAND);

    b.baseMovePower(2);
    b.costBase(PREP_COST * 4);

    b.baseMoveType(GROUND);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    WeaponModel[] weapons = { new KaijuWarsWeapons.ShootAll(3) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("OGR Platform");
    b.kaijuCounter(5);
    b.slowsLand(true);
    b.slowsAir(true);
    b.resistsKaiju(true);
    KaijuWarsUnitModel output = b.build();
    return output;
  }

}
