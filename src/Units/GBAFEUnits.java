package Units;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import Engine.GameInstance;
import Engine.UnitActionFactory;
import Engine.StateTrackers.StateTracker;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Terrain.TerrainType;
import Units.GBAFEActions.GBAFEExperienceTracker;
import Units.GBAFEActions.GBAFEStatsTracker;
import Units.GBAFEActions.SummonTracker;
import Units.MoveTypes.*;
import lombok.Builder;
import lombok.var;
import lombok.experimental.SuperBuilder;

public class GBAFEUnits extends UnitModelScheme
{
  private static final long serialVersionUID = 1L;
  private static final boolean SORT_PROMOTED_UNITS_LAST = true;
  private static final boolean USE_GROWTHS              = true;
  private static final int     PROMOTION_LEVEL_BOOST    = 19; // 7/8 Easy use 9; all others use 19

  @Override
  public String toString()
  {
    return super.toString() + "GBA Emblem";
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
    // TODO: Triangle attacks?
    GameReadyModels feModels = new GameReadyModels();

    ArrayList<UnitModel> factoryModels = new ArrayList<>();
    ArrayList<UnitModel> shipModels = new ArrayList<>();
    ArrayList<UnitModel> airportModels = new ArrayList<>();
    ArrayList<UnitModel> extras = new ArrayList<>();

    // Define everything we can build from a Factory.
    factoryModels.add(Soldier());
    var brigand = Brigand();
    factoryModels.add(brigand);
    factoryModels.add(Myrmidon());
    factoryModels.add(Thief());
    factoryModels.add(Axeman());
    factoryModels.add(Mercenary());
    var priest = Priest();
    factoryModels.add(priest);
    factoryModels.add(Troubadour());
    factoryModels.add(Mage());
    factoryModels.add(Shaman());
    var monk = Monk();
    factoryModels.add(monk);
    factoryModels.add(Nomad());
    factoryModels.add(Archer.buildModel());
    factoryModels.add(ArmorKnight());
    factoryModels.add(Cavalier());
    factoryModels.add(GreatKnight());
    factoryModels.add(Bard());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(PegKnight());
    airportModels.add(WyvernRider());
    airportModels.add(WyvernKnight());
    airportModels.add(Summoner.buildModel());

    // Record those units we can get from a Seaport.
    var pirate = Pirate();
    var fleet = Fleet();
    var siegeBoat = SiegeBoat();
    var cloister = CloisterBoat();
    shipModels.add(pirate);
    shipModels.add(fleet);
    shipModels.add(siegeBoat);
    shipModels.add(cloister);

    // Dump these lists into a hashmap for easy reference later.
    feModels.shoppingList.put(TerrainType.FACTORY, factoryModels);
    feModels.shoppingList.put(TerrainType.AIRPORT, airportModels);
    feModels.shoppingList.put(TerrainType.SEAPORT, shipModels);

    // Compile one master list of everything we can build.
    for (UnitModel um : factoryModels)
      feModels.unitModels.add(um);
    for (UnitModel um : airportModels)
      feModels.unitModels.add(um);
    for (UnitModel um : shipModels)
      feModels.unitModels.add(um);

    for (UnitModel um : extras)
      feModels.unitModels.add(um);
    for (int i = 0; i < feModels.unitModels.size(); ++i)
      ((GBAFEUnitModel) feModels.unitModels.get(i)).addVariants(feModels, i);

    // Do this after adding all the models, since we don't want two copies or entries of promoted class types
    priest.promotesTo = monk.promotesTo;
    priest.baseActions.add(new GBAFEActions.PromotionFactory(priest.promotesTo));
    pirate.promotesTo = brigand.promotesTo;
    pirate.baseActions.add(new GBAFEActions.PromotionFactory(pirate.promotesTo));

    UnitModel fleetPack = FleetPack();
    fleet.baseActions.add(new TransformLifecycle.TransformFactory(fleetPack, "PACK"));
    fleetPack.baseActions.add(new TransformLifecycle.TransformFactory(fleet, "UNPACK"));
    feModels.unitModels.add(fleetPack);
    feModels.shoppingList.get(TerrainType.SEAPORT).add(fleetPack);

    UnitModel siegeBoatPack = SiegeBoatPack();
    siegeBoat.baseActions.add(new TransformLifecycle.TransformFactory(siegeBoatPack, "PACK"));
    siegeBoatPack.baseActions.add(new TransformLifecycle.TransformFactory(siegeBoat, "UNPACK"));
    feModels.unitModels.add(siegeBoatPack);
    feModels.shoppingList.get(TerrainType.SEAPORT).add(siegeBoatPack);

    UnitModel cloisterBoatPack = CloisterBoatPack();
    cloister.baseActions.add(new TransformLifecycle.TransformFactory(cloisterBoatPack, "PACK"));
    cloisterBoatPack.baseActions.add(new TransformLifecycle.TransformFactory(cloister, "UNPACK"));
    feModels.unitModels.add(cloisterBoatPack);
    feModels.shoppingList.get(TerrainType.SEAPORT).add(cloisterBoatPack);

    // Make sure the combat modifier's on there
    var fightMod = new GBAFEWeapons.GBAFEFightMod();
    for( UnitModel um : feModels.unitModels )
      um.addUnitModifier(fightMod);

    return feModels;
  }

  public void registerStateTrackers(GameInstance gi)
  {
    super.registerStateTrackers(gi);

    var expTracker = StateTracker.instance(gi, GBAFEExperienceTracker.class);
    StateTracker.instance(gi, SummonTracker.class);
    expTracker.statsTracker = StateTracker.instance(gi, GBAFEStatsTracker.class);
  }

  public static class ClassStatsBuilder
  {
    public boolean critBoost = false;
    public boolean sureShot  = false;
    public boolean pierce    = false;
    public boolean lethality = false;
    public boolean pavise    = false;

    public int baseHP  = 0;
    public int baseStr = 0;
    public int baseSkl = 0;
    public int baseSpd = 0;
    public int baseLck = 0;
    public int baseDef = 0;
    public int baseRes = 0;

    public int growthHP  = 0;
    public int growthStr = 0;
    public int growthSkl = 0;
    public int growthSpd = 0;
    public int growthLck = 0;
    public int growthDef = 0;
    public int growthRes = 0;

    public GBAFEStats build(int levels)
    {
      return build(false, levels);
    }
    public GBAFEStats build(boolean promoted, int levels)
    {
      GBAFEStats stats = new GBAFEStats();
      stats.level    = levels+1;
      stats.promoted = promoted;
      if( promoted )
        levels += PROMOTION_LEVEL_BOOST;
      stats.critBoost = critBoost;
      stats.sureShot  = sureShot;
      stats.pierce    = pierce;
      stats.lethality = lethality;
      stats.pavise    = pavise;
      stats.HP    = baseHP ;
      stats.Str   = baseStr;
      stats.Skl   = baseSkl;
      stats.Spd   = baseSpd;
      stats.Lck   = baseLck;
      stats.Def   = baseDef;
      stats.Res   = baseRes;
      if( USE_GROWTHS )
      {
        stats.HP  += growthHP *levels/100;
        stats.Str += growthStr*levels/100;
        stats.Skl += growthSkl*levels/100;
        stats.Spd += growthSpd*levels/100;
        stats.Lck += growthLck*levels/100;
        stats.Def += growthDef*levels/100;
        stats.Res += growthRes*levels/100;
      }
      stats.growths = this;
      return stats;
    }
  }

  public static class GBAFEStats implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public boolean promoted  = false;
    public boolean critBoost = false;
    public boolean sureShot  = false;
    public boolean pierce    = false;
    public boolean lethality = false;
    public boolean pavise    = false; // Great Shield in FE8
    public int HP  = 0;
    public int Str = 0;
    public int Skl = 0;
    public int Spd = 0;
    public int Lck = 0;
    public int Def = 0;
    public int Res = 0;
    public int level = 0;
    public ClassStatsBuilder growths;
    public int calcAvoid()
    {
      return Spd*2 + Lck;
    }
    public int calcHitFromStats()
    {
      // +5 hit from having an S-rank?
      return Skl*2 + Lck/2;
    }
    public int calcCritFromStats()
    {
      // +5 crit from having an S-rank?
      return Skl/2;
    }
    public int calcCritAvoidFromStats()
    {
      return Lck;
    }
  }


  @SuperBuilder(toBuilder = true)
  public static class GBAFEUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public final GBAFEStats stats;
    @Builder.Default public boolean reducedPromoKillBonus = false;
    @Builder.Default public int classRelativePower = 3; // "Weaker" classes level faster, and "stronger" classes grant more experience
    @Builder.Default public UnitModel promotesTo = null;

    @Builder.Default public boolean isArmor   = false;
    @Builder.Default public boolean isHorse   = false;

    @Override
    public int getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }

    public void addVariants(GameReadyModels feModels, int yourIndex)
    {
      if( hidden )
        return;
      if( null != this.promotesTo )
      {
        if( SORT_PROMOTED_UNITS_LAST )
          feModels.unitModels.add(this.promotesTo);
        else
          feModels.unitModels.add(yourIndex + 1, this.promotesTo);
        this.baseActions.add(new GBAFEActions.PromotionFactory(this.promotesTo));
      }
      if( this.weapons.size() > 1 && this.weapons.get(1).rangeMax > 1 )
      {
        var rangedOnly = this.toBuilder();
        rangedOnly.hidden(true); // for visual distinctiveness
        ArrayList<WeaponModel> weapons = new ArrayList<>(this.weapons);
        weapons.remove(0);
        rangedOnly.weapons(weapons);
        feModels.unitModels.add(yourIndex+1, rangedOnly.build());
      }
    }
  }

  private static final MoveType wing       = new FEMoveTypes.FEFlight();
  private static final MoveType wheel      = new FEMoveTypes.FEWheel();
  private static final MoveType boat       = new FEMoveTypes.FEBoat();
  private static final MoveType foot       = new FEMoveTypes.FEFoot();
  private static final MoveType footPromo  = new FEMoveTypes.FEFootPlus();
  private static final MoveType footAxe    = new FEMoveTypes.FEFootAxe();
  private static final MoveType footMage   = new FEMoveTypes.FEFootMage();
  private static final MoveType footArmor  = new FEMoveTypes.FEFootArmor();
  private static final MoveType footPirate = new FEMoveTypes.FEFootPirate();
  private static final MoveType hoof       = new FEMoveTypes.FEHoof();
  private static final MoveType hoofPromo  = new FEMoveTypes.FEHoofPromoted();
  private static final MoveType hoofN      = new FEMoveTypes.FEHoofNomad();
  private static final MoveType hoofNPromo = new FEMoveTypes.FEHoofNomadPromoted();
  private static final int MAX_AMMO       = -1;
  private static final int MAX_FUEL       = 99;
  private static final int IDLE_FUEL_BURN = 0;
  public static final int VISION_NORMAL   = 3;
  public static final int VISION_THIEF    = 8;

  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> FootUnit()
  {
    var b = GBAFEUnitModel.builder();
    b.role(UnitModel.TROOP | UnitModel.LAND);
    b.baseMoveType(foot);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));

    b.maxFuel(MAX_FUEL);
    b.fuelBurnIdle(IDLE_FUEL_BURN);
    b.fuelBurnPerTile(0);
    b.needsMaterials(false);
    b.maxAmmo(MAX_AMMO);
    b.visionRange(VISION_NORMAL);
    b.baseMovePower(5);

    return b;
  }
  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> PromoFootUnit()
  {
    var b = FootUnit();
    b.baseMoveType(footPromo);
    b.baseMovePower(6);
    return b;
  }
  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> HorseUnit()
  {
    var b = FootUnit();
    b.role(UnitModel.TANK | UnitModel.LAND);
    b.baseMoveType(hoof);
    var actions = new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS));
    actions.add(1, GBAFEActions.DropUnitFactory.instance);
    actions.add(2, GBAFEActions.GiveUnitFactory.instance);
    actions.add(3, GBAFEActions.TakeUnitFactory.instance);
    actions.add(4, GBAFEActions.RescueUnitFactory.instance);
    b.baseActions(actions);
    b.isHorse(true);
    b.carryableMask(0); // The RESCUE action will be used instead of LOAD
    b.baseCargoCapacity(1);
    b.baseMovePower(7);

    return b;
  }
  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> PromoHorseUnit()
  {
    var b = HorseUnit();
    b.baseMoveType(hoofPromo);
    b.baseMovePower(8);
    return b;
  }
  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> FlierUnit()
  {
    var b = FootUnit();
    b.role(UnitModel.HOVER | UnitModel.AIR);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));
    b.baseMoveType(wing);
    b.baseMovePower(7);
    b.isHorse(false);

    return b;
  }
  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> PromoFlierUnit()
  {
    var b = FlierUnit();
    b.baseMovePower(8);
    return b;
  }
  public static GBAFEUnitModel.GBAFEUnitModelBuilder<?, ?> BoatUnit()
  {
    var b = GBAFEUnitModel.builder();
    b.baseMoveType(boat);

    b.maxFuel(MAX_FUEL);
    b.fuelBurnIdle(IDLE_FUEL_BURN);
    b.fuelBurnPerTile(0);
    b.needsMaterials(false);
    b.maxAmmo(MAX_AMMO);
    b.visionRange(VISION_NORMAL);

    b.baseCargoCapacity(2);
    b.carryableMask(UnitModel.TROOP | UnitModel.TANK | UnitModel.HOVER);

    return b;
  }

  public static GBAFEUnitModel Soldier()
  {
    var b = FootUnit();
    b.costBase(500);
    b.abilityPowerValue(2);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  3;
    bases.baseSkl =  0;
    bases.baseSpd =  1;
    bases.baseDef =  0;
    bases.baseRes =  0;
    bases.growthHP  =  80;
    bases.growthStr =  50;
    bases.growthSkl =  30;
    bases.growthSpd =  20;
    bases.growthLck =  12; // 25 in 7/8
    bases.growthDef =  10; // 12 in 7/8
    bases.growthRes =  25; // 15 in 7/8
    var static_stats = bases.build(4);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Soldier");
    b.promotesTo(Halberdier());
    return b.build();
  }
  public static GBAFEUnitModel Halberdier()
  {
    var b = PromoFootUnit(); // PoR standardizes on 7 move; might be amusing
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(22000);
    b.abilityPowerValue(22);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.critBoost = true; // based on fangames/Radiant Dawn
    // Stats are: PoR// + FE8 zerker - PoR zerker
    bases.baseHP  = 24;// + 24 - 28;
    bases.baseStr =  6;// + 7 - 8;
    bases.baseSkl =  6;// + 5 + 6;
    bases.baseSpd =  6;
    bases.baseDef =  6;
    bases.baseRes =  2;
    bases.growthHP  =  75;// + 75 - 80;
    bases.growthStr =  45;// + 50 - 60;
    bases.growthSkl =  45;// + 35 - 25;
    bases.growthSpd =  40;// + 25 - 40;
    bases.growthLck =  25;// + 15 - 25;
    bases.growthDef =  45;// + 10 - 35;
    bases.growthRes =  40;// + 13 - 15;
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats), new GBAFEWeapons.Javelin(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Halberdier");
    return b.build();
  }

  public static GBAFEUnitModel Brigand()
  {
    var b = FootUnit();
    b.costBase(500);
    b.abilityPowerValue(4);
    MoveType myFoot = footAxe.clone(); // Peaks don't exist
    myFoot.setMoveCost(TerrainType.RIVER, 5);
    b.baseMoveType(myFoot);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  5;
    bases.baseSkl =  1;
    bases.baseSpd =  5;
    bases.baseDef =  3;
    bases.baseRes =  0;
    bases.growthHP  =  82;
    bases.growthStr =  50;
    bases.growthSkl =  30;
    bases.growthSpd =  20;
    bases.growthLck =  15;
    bases.growthDef =  10;
    bases.growthRes =  10; // 13 in 7/8
    var static_stats = bases.build(0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronAxe(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Brigand");
    b.promotesTo(Berserker());
    return b.build();
  }
  public static GBAFEUnitModel Berserker()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(11000);
    b.abilityPowerValue(12);
    MoveType myFoot = footPirate.clone();
    myFoot.setMoveCost(TerrainType.DUNES, 3);
    myFoot.setMoveCost(TerrainType.MOUNTAIN, 3);
    b.baseMoveType(myFoot);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.critBoost = true;
    bases.baseHP  = 24;
    bases.baseStr =  7;
    bases.baseSkl =  6;
    bases.baseSpd =  7;
    bases.baseDef =  6;
    bases.baseRes =  0;
    bases.growthHP  =  75; // 58 in 6
    bases.growthStr =  50; // 35 in 6
    bases.growthSkl =  35; // 25 in 6
    bases.growthSpd =  25; // 12 in 6
    bases.growthLck =  15;
    bases.growthDef =  10;
    bases.growthRes =  13; // 10 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.KillerAxe(static_stats), new GBAFEWeapons.HandAxe(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Berserker");
    return b.build();
  }

  public static GBAFEUnitModel Myrmidon()
  {
    var b = FootUnit();
    b.costBase(2500);
    b.abilityPowerValue(6);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 16;
    bases.baseStr =  4;
    bases.baseSkl =  9;
    bases.baseSpd =  9;
    bases.baseDef =  2;
    bases.baseRes =  0;
    bases.growthHP  =  70;
    bases.growthStr =  35;
    bases.growthSkl =  40;
    bases.growthSpd =  40;
    bases.growthLck =  30;
    bases.growthDef =  15;
    bases.growthRes =  20; // 17 in 6
    var static_stats = bases.build(4);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronSword(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Myrmidon");
    b.promotesTo(Swordmaster());
    return b.build();
  }
  public static GBAFEUnitModel Swordmaster()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(13000);
    b.abilityPowerValue(10);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.critBoost = true;
    bases.baseHP  = 21; // 19 female
    bases.baseStr =  6;
    bases.baseSkl = 11;
    bases.baseSpd = 10; // 12 female
    bases.baseDef =  5; // 4 female
    bases.baseRes =  2; // 3 female
    bases.growthHP  =  65;
    bases.growthStr =  25; // 20 male in 6
    bases.growthSkl =  30;
    bases.growthSpd =  30;
    bases.growthLck =  25; // 15 in 6
    bases.growthDef =  15; // 12 in 6
    bases.growthRes =  22; // 25 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.KillingEdge(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Swordmaster");
    return b.build();
  }

  public static GBAFEUnitModel Thief()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.RECON);
    b.costBase(2500);
    b.baseMovePower(6);
    b.baseMoveType(footPromo);
    b.abilityPowerValue(6);
    b.visionRange(VISION_THIEF);
    b.classRelativePower(2);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 16;
    bases.baseStr =  3;
    bases.baseSkl =  1;
    bases.baseSpd =  9;
    bases.baseDef =  2;
    bases.baseRes =  0;
    bases.growthHP  =  50;
    bases.growthStr =   5;
    bases.growthSkl =  45;
    bases.growthSpd =  40;
    bases.growthLck =  40;
    bases.growthDef =   5;
    bases.growthRes =  20;
    var static_stats = bases.build(6);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronSword(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Thief");
    b.promotesTo(Assassin());
    return b.build();
  }
  public static GBAFEUnitModel Assassin()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.RECON | UnitModel.ASSAULT);
    b.costBase(13000);
    b.abilityPowerValue(10);
    b.visionRange(VISION_THIEF);
    b.reducedPromoKillBonus(true);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.lethality = true;
    bases.baseHP  = 16 + 2; // Adding Marisa's promo gains since the class has Thief bases
    bases.baseStr =  3 + 1;
    bases.baseSkl =  1 + 1;
    bases.baseSpd =  9 + 1;
    bases.baseDef =  2 + 2;
    bases.baseRes =  0 + 1;
    bases.growthHP  =  50;
    bases.growthStr =   5;
    bases.growthSkl =  45;
    bases.growthSpd =  40;
    bases.growthLck =  40;
    bases.growthDef =   5;
    bases.growthRes =  20;
    var static_stats = bases.build(true, 19); // Blatant favoritism, and barely enough
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.KillingEdge(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Assassin");
    return b.build();
  }

  // "Fighter" means something different here...
  public static GBAFEUnitModel Axeman()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.MECH | UnitModel.ASSAULT);
    b.costBase(4000);
    b.abilityPowerValue(8);
    b.baseMoveType(footAxe);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  5;
    bases.baseSkl =  2;
    bases.baseSpd =  4;
    bases.baseDef =  2;
    bases.baseRes =  0;
    bases.growthHP  =  85;
    bases.growthStr =  55; // 35 in 7
    bases.growthSkl =  35; // 30 in 7
    bases.growthSpd =  30; // 20 in 7
    bases.growthLck =  15;
    bases.growthDef =  15;
    bases.growthRes =  15; // 10 in 6
    var static_stats = bases.build(6);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Hammer(static_stats), new GBAFEWeapons.IronAxe(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Axeman");
    b.promotesTo(Warrior());
    return b.build();
  }
  public static GBAFEUnitModel Warrior()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.MECH | UnitModel.ASSAULT);
    b.costBase(12000);
    b.abilityPowerValue(10);
    b.baseMoveType(footAxe);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 28;
    bases.baseStr =  8;
    bases.baseSkl =  5;
    bases.baseSpd =  6;
    bases.baseDef =  5;
    bases.baseRes =  0;
    bases.growthHP  =  80;
    bases.growthStr =  45;
    bases.growthSkl =  25;
    bases.growthSpd =  20;
    bases.growthLck =  15;
    bases.growthDef =  16;
    bases.growthRes =  17; // 7 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Hammer(static_stats), new GBAFEWeapons.IronBow(static_stats), new GBAFEWeapons.IronAxe(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Warrior");
    return b.build();
  }

  public static GBAFEUnitModel Mercenary()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(6000);
    b.abilityPowerValue(8);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 18;
    bases.baseStr =  4;
    bases.baseSkl =  8;
    bases.baseSpd =  8;
    bases.baseDef =  4;
    bases.baseRes =  0;
    bases.growthHP  =  80;
    bases.growthStr =  40;
    bases.growthSkl =  40;
    bases.growthSpd =  32;
    bases.growthLck =  30;
    bases.growthDef =  18;
    bases.growthRes =  20; // 15 in 6, 30 female
    var static_stats = bases.build(9);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronSword(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Mercenary");
    b.promotesTo(Hero());
    return b.build();
  }
  public static GBAFEUnitModel Hero()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(16000);
    b.abilityPowerValue(16);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 22;
    bases.baseStr =  6;
    bases.baseSkl =  9;
    bases.baseSpd = 10;
    bases.baseDef =  8;
    bases.baseRes =  2;
    bases.growthHP  =  75;
    bases.growthStr =  30;
    bases.growthSkl =  30;
    bases.growthSpd =  20;
    bases.growthLck =  25;
    bases.growthDef =  20;
    bases.growthRes =  20; // 10 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronHero(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Hero");
    return b.build();
  }

  public static GBAFEUnitModel ArmorKnight()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.MECH);
    b.costBase(8000);
    b.abilityPowerValue(12);
    b.baseMoveType(footArmor);
    b.baseMovePower(4);
    b.isArmor(true);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 17;
    bases.baseStr =  5;
    bases.baseSkl =  2;
    bases.baseSpd =  0;
    bases.baseDef =  9;
    bases.baseRes =  0;
    bases.growthHP  =  80;
    bases.growthStr =  40;
    bases.growthSkl =  30; // 35 in 6
    bases.growthSpd =  15;
    bases.growthLck =  25;
    bases.growthDef =  28;
    bases.growthRes =  20; // 5 in 6
    var static_stats = bases.build(9);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Horseslayer(static_stats), new GBAFEWeapons.IronLance(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Armor Knight");
    b.promotesTo(General());
    return b.build();
  }
  public static GBAFEUnitModel General()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.MECH | UnitModel.ASSAULT);
    b.costBase(16000);
    b.abilityPowerValue(10);
    b.baseMoveType(footArmor);
    b.baseMovePower(5);
    b.isArmor(true);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.pavise = true;
    bases.baseHP  = 21;
    bases.baseStr =  8;
    bases.baseSkl =  4;
    bases.baseSpd =  3;
    bases.baseDef = 13;
    bases.baseRes =  3;
    bases.growthHP  =  75;
    bases.growthStr =  30;
    bases.growthSkl =  20; // 25/35 in 6, 25 female
    bases.growthSpd =  10;
    bases.growthLck =  20;
    bases.growthDef =  23;
    bases.growthRes =  25; // 15 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.GeneralWeapons(static_stats), new GBAFEWeapons.Javelina(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("General");
    return b.build();
  }

  public static GBAFEUnitModel GreatKnight()
  {
    var b = PromoHorseUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(22000);
    b.abilityPowerValue(10);
    b.baseMovePower(6);
    b.isArmor(true);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 21;
    bases.baseStr =  8;
    bases.baseSkl =  4;
    bases.baseSpd =  6;
    bases.baseDef = 11;
    bases.baseRes =  3;
    bases.growthHP  =  70;
    bases.growthStr =  30;
    bases.growthSkl =  20;
    bases.growthSpd =  15;
    bases.growthLck =  20;
    bases.growthDef =  21;
    bases.growthRes =  20;
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronTriangle(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Great Knight");
    return b.build();
  }

  public static GBAFEUnitModel Nomad()
  {
    var b = HorseUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.SURFACE_TO_AIR);
    b.costBase(4000);
    b.abilityPowerValue(12);
    b.baseMoveType(hoofN);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 16;
    bases.baseStr =  5;
    bases.baseSkl =  4;
    bases.baseSpd =  5;
    bases.baseDef =  4;
    bases.baseRes =  0;
    bases.growthHP  =  65;
    bases.growthStr =  30;
    bases.growthSkl =  40;
    bases.growthSpd =  45;
    bases.growthLck =  30;
    bases.growthDef =  12;
    bases.growthRes =  15;
    var static_stats = bases.build(2);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronBow(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Nomad");
    b.promotesTo(NomadTrooper());
    return b.build();
  }
  public static GBAFEUnitModel NomadTrooper()
  {
    var b = PromoHorseUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.ASSAULT | UnitModel.SURFACE_TO_AIR);
    b.costBase(10000);
    b.abilityPowerValue(10);
    b.baseMoveType(hoofNPromo);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 21;
    bases.baseStr =  7;
    bases.baseSkl =  6;
    bases.baseSpd =  7;
    bases.baseDef =  6;
    bases.baseRes =  3;
    bases.growthHP  =  60;
    bases.growthStr =  25;
    bases.growthSkl =  30;
    bases.growthSpd =  35;
    bases.growthLck =  25;
    bases.growthDef =  15;
    bases.growthRes =  15; // 10 in 6, 20 female in 7
    var static_stats = bases.build(true, 0); // The idea is "promo gains and killer weapons only"
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.KillingEdge(static_stats), new GBAFEWeapons.KillerBow(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Nomad Trooper");
    return b.build();
  }

  public static GBAFEUnitModel ArcherInBallista()
  {
    var b = FootUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.SURFACE_TO_AIR);
    b.costBase(6000);
    b.abilityPowerValue(6);
    b.baseMoveType(wheel);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));

    var static_stats = Archer.buildStats();
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Ballista(static_stats) };
    b.maxAmmo(5);
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Ballista");
    GBAFEUnitModel model = b.build();
    model.healableHabs.clear(); // No free resupplies for you
    return model;
  }
  @SuperBuilder(toBuilder = true)
  public static class Archer extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;

    public static GBAFEUnitModel buildModel()
    {
      var b = Archer.builder();
      b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.SURFACE_TO_AIR);
      b.baseMoveType(foot);
      b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));

      b.maxFuel(MAX_FUEL);
      b.fuelBurnIdle(IDLE_FUEL_BURN);
      b.fuelBurnPerTile(0);
      b.needsMaterials(false);
      b.maxAmmo(MAX_AMMO);
      b.visionRange(VISION_NORMAL);
      b.baseMovePower(5);

      b.costBase(6000);
      b.abilityPowerValue(8);

      var static_stats = Archer.buildStats();
      b.stats(static_stats);
      WeaponModel[] weapons = { new GBAFEWeapons.IronBow(static_stats), new GBAFEWeapons.Longbow(static_stats) };
      b.weapons(new ArrayList<>(Arrays.asList(weapons)));

      b.name("Archer");
      b.promotesTo(Sniper.buildModel());
      return b.build();
    }

    public static GBAFEStats buildStats()
    {
      ClassStatsBuilder bases = new ClassStatsBuilder();
      bases.baseHP  = 18;
      bases.baseStr =  4;
      bases.baseSkl =  3;
      bases.baseSpd =  3;
      bases.baseDef =  3;
      bases.baseRes =  0;
      bases.growthHP  =  70;
      bases.growthStr =  35;
      bases.growthSkl =  40;
      bases.growthSpd =  32;
      bases.growthLck =  35;
      bases.growthDef =  15;
      bases.growthRes =  20; // 10 in 6, 15 female
      return bases.build(9); // Wow, this class's stats are bad
    }

    @Override
    public void addVariants(GameReadyModels feModels, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(feModels, yourIndex);
        UnitModel ballista = ArcherInBallista();
        baseActions.add(new GBAFEActions.PromotionFactory(ballista, GBAFEActions.BALLISTA_COST));
        ballista.baseActions.add(new TransformLifecycle.TransformFactory(this, "DISMOUNT"));
        feModels.unitModels.add(yourIndex + 2, ballista);
      }
    }
  }
  public static GBAFEUnitModel SniperInBallista()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.SURFACE_TO_AIR);
    b.costBase(12000);
    b.abilityPowerValue(6);
    b.baseMoveType(wheel);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));

    var static_stats = Sniper.buildStats();
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.KillerBallista(static_stats) };
    b.maxAmmo(5);
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Killer Ballista");
    GBAFEUnitModel model = b.build();
    model.healableHabs.clear(); // No free resupplies for you
    return model;
  }
  @SuperBuilder(toBuilder = true)
  public static class Sniper extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;

    public static GBAFEUnitModel buildModel()
    {
      var b = Sniper.builder();
      b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.SURFACE_TO_AIR);
      b.baseMoveType(footPromo);
      b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));

      b.maxFuel(MAX_FUEL);
      b.fuelBurnIdle(IDLE_FUEL_BURN);
      b.fuelBurnPerTile(0);
      b.needsMaterials(false);
      b.maxAmmo(MAX_AMMO);
      b.visionRange(VISION_NORMAL);
      b.baseMovePower(6);

      b.costBase(12000);
      b.abilityPowerValue(10);

      var static_stats = Sniper.buildStats();
      b.stats(static_stats);
      WeaponModel[] weapons = { new GBAFEWeapons.KillerBow(static_stats), new GBAFEWeapons.Longbow(static_stats) };
      b.weapons(new ArrayList<>(Arrays.asList(weapons)));

      b.name("Sniper");
      return b.build();
    }

    private static GBAFEStats buildStats()
    {
      ClassStatsBuilder bases = new ClassStatsBuilder();
      bases.sureShot = true;
      bases.baseHP  = 21;
      bases.baseStr =  7;
      bases.baseSkl =  6;
      bases.baseSpd =  5;
      bases.baseDef =  5;
      bases.baseRes =  2;
      bases.growthHP  =  65;
      bases.growthStr =  30;
      bases.growthSkl =  30;
      bases.growthSpd =  20;
      bases.growthLck =  30;
      bases.growthDef =  15;
      bases.growthRes =  20; // 15 in 6
      return bases.build(true, 0);
    }

    @Override
    public void addVariants(GameReadyModels feModels, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(feModels, yourIndex);
        UnitModel killerBallista = SniperInBallista();
        baseActions.add(new GBAFEActions.PromotionFactory(killerBallista, GBAFEActions.KILLER_BALLISTA_COST));
        killerBallista   .baseActions.add(new TransformLifecycle.TransformFactory(this, "DISMOUNT"));
        feModels.unitModels.add(yourIndex + 2, killerBallista);
      }
    }
  }

  public static GBAFEUnitModel Priest()
  {
    var b = FootUnit();
    b.costBase(2000);
    b.abilityPowerValue(8);
    b.baseMoveType(footMage);
    b.classRelativePower(2);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 18;
    bases.baseStr =  1;
    bases.baseSkl =  1;
    bases.baseSpd =  2;
    bases.baseDef =  1;
    bases.baseRes =  5;
    bases.growthHP  =  50;
    bases.growthStr =  30;
    bases.growthSkl =  35;
    bases.growthSpd =  32;
    bases.growthLck =  45;
    bases.growthDef =   8;
    bases.growthRes =  50;
    var static_stats = bases.build(6);
    b.stats(static_stats);
    WeaponModel[] weapons = {};
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Priest");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, new GBAFEActions.HealStaffFactory("HEAL (3)", 3, 1));
    return model;
  }

  public static GBAFEUnitModel Troubadour()
  {
    var b = HorseUnit();
    b.costBase(4000);
    b.abilityPowerValue(8);
    b.baseMoveType(footMage);
    b.classRelativePower(2);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 15;
    bases.baseStr =  1;
    bases.baseSkl =  1;
    bases.baseSpd =  3;
    bases.baseDef =  2;
    bases.baseRes =  5;
    bases.growthHP  =  50;
    bases.growthStr =  25;
    bases.growthSkl =  35;
    bases.growthSpd =  55;
    bases.growthLck =  45; // 12 in 6
    bases.growthDef =  12; // 30 in 6
    bases.growthRes =  40; // 45 in 6
    var static_stats = bases.build(9);
    b.stats(static_stats);
    WeaponModel[] weapons = {};
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Troubadour");
    b.promotesTo(Valkyrie());
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, new GBAFEActions.HealStaffFactory("HEAL (5)", 5, 1));
    return model;
  }
  public static GBAFEUnitModel Valkyrie()
  {
    var b = PromoHorseUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(11000);
    b.abilityPowerValue(20);
    b.reducedPromoKillBonus(true);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 19;
    bases.baseStr =  4;
    bases.baseSkl =  3;
    bases.baseSpd =  5;
    bases.baseDef =  4;
    bases.baseRes =  8;
    bases.growthHP  =  45;
    bases.growthStr =  35;
    bases.growthSkl =  25;
    bases.growthSpd =  45;
    bases.growthLck =  40;
    bases.growthDef =  10;
    bases.growthRes =  40; // 20 in 6, but 8's uses light, so IDK
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Valkyrie");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, new GBAFEActions.HealStaffFactory("HEAL (7)", 7, 1));
    return model;
  }

  public static GBAFEUnitModel Mage()
  {
    var b = FootUnit();
    b.costBase(2000);
    b.abilityPowerValue(6);
    b.baseMoveType(footMage);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 16;
    bases.baseStr =  1;
    bases.baseSkl =  2;
    bases.baseSpd =  3;
    bases.baseDef =  3;
    bases.baseRes =  3;
    bases.growthHP  =  55;
    bases.growthStr =  55;
    bases.growthSkl =  40;
    bases.growthSpd =  35;
    bases.growthLck =  20;
    bases.growthDef =   5;
    bases.growthRes =  30; // 35 in 6, 40 female
    var static_stats = bases.build(3);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Mage");
    b.promotesTo(Sage());
    return b.build();
  }
  public static GBAFEUnitModel Sage()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(30000);
    b.abilityPowerValue(30);
    b.baseMoveType(footMage);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  5;
    bases.baseSkl =  4;
    bases.baseSpd =  4;
    bases.baseDef =  5;
    bases.baseRes =  5;
    bases.growthHP  =  45;
    bases.growthStr =  45;
    bases.growthSkl =  30;
    bases.growthSpd =  25;
    bases.growthLck =  15;
    bases.growthDef =  10;
    bases.growthRes =  40; // 25 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats), new GBAFEWeapons.Bolting(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Sage");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, new GBAFEActions.HealStaffFactory("HEAL (7)", 7, 1));
    return model;
  }

  public static GBAFEUnitModel Shaman()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(5000);
    b.abilityPowerValue(8);
    b.baseMoveType(footMage);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 16;
    bases.baseStr =  2;
    bases.baseSkl =  1;
    bases.baseSpd =  2;
    bases.baseDef =  2;
    bases.baseRes =  4;
    bases.growthHP  =  50;
    bases.growthStr =  50; // 45 female
    bases.growthSkl =  32;
    bases.growthSpd =  30;
    bases.growthLck =  20;
    bases.growthDef =  10;
    bases.growthRes =  30; // 37 in 6, 40 female
    var static_stats = bases.build(7);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Flux(static_stats), new GBAFEWeapons.Luna(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Shaman");
    b.promotesTo(Druid());
    return b.build();
  }
  public static GBAFEUnitModel Druid()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(16000);
    b.abilityPowerValue(16);
    b.baseMoveType(footMage);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 19;
    bases.baseStr =  6;
    bases.baseSkl =  3;
    bases.baseSpd =  4;
    bases.baseDef =  4;
    bases.baseRes =  6;
    bases.growthHP  =  45;
    bases.growthStr =  55;
    bases.growthSkl =  30;
    bases.growthSpd =  25;
    bases.growthLck =  20;
    bases.growthDef =  10;
    bases.growthRes =  35; // 37 in 6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Flux(static_stats), new GBAFEWeapons.Luna(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Druid");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, new GBAFEActions.HealStaffFactory("HEAL (7)", 7, 1));
    return model;
  }

  public static GBAFEUnitModel Monk()
  {
    var b = FootUnit();
    b.costBase(6000);
    b.abilityPowerValue(8);
    b.baseMoveType(footMage);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 18;
    bases.baseStr =  1;
    bases.baseSkl =  1;
    bases.baseSpd =  2;
    bases.baseDef =  1;
    bases.baseRes =  5;
    bases.growthHP  =  50;
    bases.growthStr =  30;
    bases.growthSkl =  35;
    bases.growthSpd =  32;
    bases.growthLck =  45;
    bases.growthDef =   8;
    bases.growthRes =  40;
    var static_stats = bases.build(9);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Lightning(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Monk");
    b.promotesTo(Bishop());
    return b.build();
  }
  public static GBAFEUnitModel Bishop()
  {
    var b = PromoFootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(13000);
    b.abilityPowerValue(30);
    b.baseMoveType(footMage);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 21;
    bases.baseStr =  4;
    bases.baseSkl =  4;
    bases.baseSpd =  4;
    bases.baseDef =  3;
    bases.baseRes =  8;
    bases.growthHP  =  45;
    bases.growthStr =  35;
    bases.growthSkl =  25;
    bases.growthSpd =  22;
    bases.growthLck =  40;
    bases.growthDef =   8;
    bases.growthRes =  40;
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Lightning(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Bishop");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(1, new GBAFEActions.HealStaffFactory("PHYSIC (7)", 7, 5));
    return model;
  }

  public static GBAFEUnitModel Cavalier()
  {
    var b = HorseUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(10000);
    b.abilityPowerValue(12);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  5;
    bases.baseSkl =  2;
    bases.baseSpd =  5;
    bases.baseDef =  6;
    bases.baseRes =  0;
    bases.growthHP  =  75;
    bases.growthStr =  35;
    bases.growthSkl =  40;
    bases.growthSpd =  28;
    bases.growthLck =  30;
    bases.growthDef =  15;
    bases.growthRes =  12; // 15 in 7/8
    var static_stats = bases.build(9);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Cavalier");
    b.promotesTo(Paladin());
    return b.build();
  }
  public static GBAFEUnitModel Paladin()
  {
    var b = PromoHorseUnit();
    b.role(UnitModel.TANK | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(16000);
    b.abilityPowerValue(10);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 23;
    bases.baseStr =  7; // Female paladins are bizarrely good? -1 Str/Def for 2 Skl, 4 Spd, 3 Res
    bases.baseSkl =  4;
    bases.baseSpd =  7;
    bases.baseDef =  8;
    bases.baseRes =  3;
    bases.growthHP  =  70;
    bases.growthStr =  25;
    bases.growthSkl =  30; // 35 for female
    bases.growthSpd =  18; // 25 for female
    bases.growthLck =  25;
    bases.growthDef =  12;
    bases.growthRes =  20; // 17 in 6, 25 for female
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Paladin");
    GBAFEUnitModel model = b.build();
    return model;
  }

  public static GBAFEUnitModel Bard()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(13000);
    b.abilityPowerValue(20);
    b.classRelativePower(2);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 14;
    bases.baseStr =  1;
    bases.baseSkl =  2;
    bases.baseSpd =  7;
    bases.baseDef =  1;
    bases.baseRes =  0;
    bases.growthHP  =  45;
    bases.growthStr =  45;
    bases.growthSkl =  30;
    bases.growthSpd =  60;
    bases.growthLck =  70;
    bases.growthDef =   0;
    bases.growthRes =  13; // 3 in 6
    var static_stats = bases.build(9);
    b.stats(static_stats);

    b.name("Bard");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, new GBAFEActions.ReactivateUnitFactory("PLAY"));
    return model;
  }

  public static GBAFEUnitModel PegKnight()
  {
    var b = FlierUnit();
    b.costBase(4000);
    b.abilityPowerValue(10);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 14;
    bases.baseStr =  4;
    bases.baseSkl =  5;
    bases.baseSpd =  5;
    bases.baseDef =  3;
    bases.baseRes =  2;
    bases.growthHP  =  65;
    bases.growthStr =  35;
    bases.growthSkl =  40;
    bases.growthSpd =  40;
    bases.growthLck =  35; // 12 in FE6
    bases.growthDef =  12; // 25 in FE6
    bases.growthRes =  35;
    var static_stats = bases.build(3);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats), new GBAFEWeapons.Javelin(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Pegasus Knight");
    b.promotesTo(FalcoKnight());
    return b.build();
  }
  public static GBAFEUnitModel FalcoKnight()
  {
    var b = PromoFlierUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT | UnitModel.AIR_TO_AIR | UnitModel.AIR_TO_SURFACE);
    b.costBase(12000);
    b.abilityPowerValue(12);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  6;
    bases.baseSkl =  7;
    bases.baseSpd =  7;
    bases.baseDef =  5;
    bases.baseRes =  4;
    bases.growthHP  =  60;
    bases.growthStr =  30;
    bases.growthSkl =  30;
    bases.growthSpd =  30;
    bases.growthLck =  30;
    bases.growthDef =  12;
    bases.growthRes =  30; // 20 in FE6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats), new GBAFEWeapons.Javelin(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Falco Knight");
    GBAFEUnitModel model = b.build();
    return model;
  }

  public static GBAFEUnitModel WyvernRider()
  {
    var b = FlierUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT | UnitModel.AIR_TO_AIR | UnitModel.AIR_TO_SURFACE);
    b.costBase(7000);
    b.abilityPowerValue(10);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 20;
    bases.baseStr =  7;
    bases.baseSkl =  3;
    bases.baseSpd =  5;
    bases.baseDef =  8;
    bases.baseRes =  0;
    bases.growthHP  =  80; // 75 in 7
    bases.growthStr =  45; // 40 in 7
    bases.growthSkl =  35; // 30 in 7
    bases.growthSpd =  30; // 20 in 7
    bases.growthLck =  25; // 20 in 7
    bases.growthDef =  25; // 20 in 7
    bases.growthRes =  15; // 10/17/15/17 in 6/7/8/female 8
    var static_stats = bases.build(6);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Wyvern Rider");
    b.promotesTo(WyvernLord());
    return b.build();
  }
  public static GBAFEUnitModel WyvernLord()
  {
    var b = PromoFlierUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT | UnitModel.AIR_TO_AIR | UnitModel.AIR_TO_SURFACE);
    b.costBase(12000);
    b.abilityPowerValue(12);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 25;
    bases.baseStr =  9;
    bases.baseSkl =  5;
    bases.baseSpd =  7;
    bases.baseDef = 10;
    bases.baseRes =  1;
    bases.growthHP  =  75;
    bases.growthStr =  40;
    bases.growthSkl =  30;
    bases.growthSpd =  20;
    bases.growthLck =  20;
    bases.growthDef =  20;
    bases.growthRes =  17; // 7 in FE6
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Wyvern Lord");
    GBAFEUnitModel model = b.build();
    return model;
  }

  public static GBAFEUnitModel WyvernKnight()
  {
    var b = PromoFlierUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT | UnitModel.AIR_TO_AIR | UnitModel.AIR_TO_SURFACE);
    b.costBase(18000);
    b.abilityPowerValue(16);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.pierce = true;
    bases.baseHP  = 20;
    bases.baseStr =  7;
    bases.baseSkl =  7;
    bases.baseSpd =  8;
    bases.baseDef =  7;
    bases.baseRes =  1;
    bases.growthHP  =  65;
    bases.growthStr =  35;
    bases.growthSkl =  30;
    bases.growthSpd =  30;
    bases.growthLck =  25;
    bases.growthDef =  15;
    bases.growthRes =  17;
    var static_stats = bases.build(true, 0);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Wyvern Knight");
    GBAFEUnitModel model = b.build();
    return model;
  }

  @SuperBuilder(toBuilder = true)
  public static class Summoner extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;

    public static GBAFEUnitModel buildModel()
    {
      var b = Summoner.builder();
      b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
      b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.FOOTSOLDIER_ACTIONS)));

      b.maxFuel(MAX_FUEL);
      b.fuelBurnIdle(IDLE_FUEL_BURN);
      b.maxAmmo(MAX_AMMO);
      b.visionRange(VISION_NORMAL);
      b.baseMovePower(6);

      b.costBase(22000);
      b.abilityPowerValue(26);
      b.baseMoveType(footMage);

      var static_stats = Summoner.buildStats();
      b.stats(static_stats);
      WeaponModel[] weapons = { new GBAFEWeapons.Flux(static_stats) };
      b.weapons(new ArrayList<>(Arrays.asList(weapons)));

      b.name("Summoner");
      GBAFEUnitModel model = b.build();
      model.baseActions.add(1, new GBAFEActions.HealStaffFactory("HEAL (5)", 5, 1));
      return model;
    }

    private static GBAFEStats buildStats()
    {
      ClassStatsBuilder bases = new ClassStatsBuilder();
      bases.baseHP  = 18;
      bases.baseStr =  5;
      bases.baseSkl =  3;
      bases.baseSpd =  4;
      bases.baseDef =  3;
      bases.baseRes =  5;
      bases.growthHP  =  45;
      bases.growthStr =  50;
      bases.growthSkl =  30;
      bases.growthSpd =  25;
      bases.growthLck =  20;
      bases.growthDef =   8;
      bases.growthRes =  35;
      return bases.build(true, 0);
    }

    @Override
    public void addVariants(GameReadyModels feModels, int yourIndex)
    {
      super.addVariants(feModels, yourIndex);
      UnitModel summon = Phantom();
      baseActions.add(1, new GBAFEActions.SummonPhantomFactory(summon));
      feModels.unitModels.add(yourIndex + 1, summon);
    }
  }
  public static GBAFEUnitModel Phantom()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(0);
    b.abilityPowerValue(0);
    b.baseMoveType(wing);
    // Phantoms shouldn't capture. That's... not kosher.
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));

    ClassStatsBuilder bases = new ClassStatsBuilder();
    // Lyon's phantom stats
    bases.baseHP  =  1;
    bases.baseStr =  8;
    bases.baseSkl =  4;
    bases.baseSpd =  7;
    bases.baseLck = 20; // !?
    bases.baseDef =  0;
    bases.baseRes =  0;
    bases.growthStr =  60;
    bases.growthSkl =  45;
    bases.growthSpd =  30;
    bases.growthLck =  60;
    bases.growthDef =  0; // Funnily enough, the Phantom *class* has a 15% Def/Res growth.
    bases.growthRes =  0;
    var static_stats = bases.build(4);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronAxe(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Phantom");
    GBAFEUnitModel model = b.build();
    model.healableHabs.clear();
    return model;
  }

  public static GBAFEUnitModel Pirate()
  {
    var b = FootUnit();
    b.role(UnitModel.TROOP | UnitModel.LAND | UnitModel.ASSAULT);
    b.costBase(5000);
    b.abilityPowerValue(10);
    b.baseMoveType(footPirate);

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 19;
    bases.baseStr =  4;
    bases.baseSkl =  2;
    bases.baseSpd =  6;
    bases.baseDef =  3;
    bases.baseRes =  0;
    bases.growthHP  =  75;
    bases.growthStr =  50;
    bases.growthSkl =  35;
    bases.growthSpd =  25;
    bases.growthLck =  10; // 15 in 7/8
    bases.growthDef =  10;
    bases.growthRes =  15; // 13 in 7/8
    var static_stats = bases.build(5);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.IronAxe(static_stats), new GBAFEWeapons.HandAxe(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Pirate");
    // Promotion is set by the caller
    return b.build();
  }

  public static GBAFEUnitModel FleetPack()
  {
    var b = BoatUnit();
    b.role(UnitModel.SHIP | UnitModel.SEA | UnitModel.SURFACE_TO_AIR);
    b.costBase(8000);
    b.abilityPowerValue(18);
    b.baseMovePower(20);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 19;
    bases.baseStr =  1;
    bases.baseSkl =  1;
    bases.baseSpd =  2;
    bases.baseDef =  5;
    bases.baseRes =  0;
    bases.growthHP  =  75;
    bases.growthStr =  35;
    bases.growthSkl =  40;
    bases.growthSpd =  32;
    bases.growthLck =  15;
    bases.growthDef =  15;
    bases.growthRes =  15;
    var static_stats = bases.build(9);
    b.stats(static_stats);
    b.maxAmmo(5);

    b.name("Fleet Pack");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, UnitActionFactory.LAUNCH);
    return model;
  }
  public static GBAFEUnitModel Fleet()
  {
    var b = FleetPack().toBuilder();
    b.name("Fleet");
    b.baseMovePower(3);

    GBAFEUnitModel model = b.build();
    var static_stats = model.stats;
    model.weapons.add(new GBAFEWeapons.Ballista(static_stats));
    model.baseActions.add(1, UnitActionFactory.ATTACK);
    return model;
  }

  public static GBAFEUnitModel SiegeBoatPack()
  {
    var b = BoatUnit();
    b.role(UnitModel.SHIP | UnitModel.SEA);
    b.costBase(13000);
    b.abilityPowerValue(18);
    b.baseMovePower(20);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.TRANSPORT_ACTIONS)));

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 19;
    bases.baseStr =  1;
    bases.baseSkl =  1;
    bases.baseSpd =  2;
    bases.baseDef =  8; // Armor boat gooo
    bases.baseRes =  0;
    bases.growthHP  =  75;
    bases.growthStr =  35;
    bases.growthSkl =  40;
    bases.growthSpd =  32;
    bases.growthLck =  15;
    bases.growthDef =  15;
    bases.growthRes =  15;
    var static_stats = bases.build(14);
    b.stats(static_stats);
    b.maxAmmo(5);

    b.name("Siege Boat pack");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, UnitActionFactory.LAUNCH);
    return model;
  }
  public static GBAFEUnitModel SiegeBoat()
  {
    var b = SiegeBoatPack().toBuilder();
    b.name("Siege Boat");
    b.baseMovePower(3);

    GBAFEUnitModel model = b.build();
    var static_stats = model.stats;
    model.weapons.add(new GBAFEWeapons.Trebuchet(static_stats));
    model.baseActions.add(1, UnitActionFactory.ATTACK);
    return model;
  }

  public static GBAFEUnitModel CloisterBoatPack()
  {
    var b = BoatUnit();
    b.role(UnitModel.SHIP | UnitModel.SEA);
    b.costBase(20000);
    b.abilityPowerValue(18);
    b.baseMovePower(20);
    b.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_TRANSPORT_ACTIONS)));

    ClassStatsBuilder bases = new ClassStatsBuilder();
    bases.baseHP  = 19;
    bases.baseStr =  1;
    bases.baseSkl =  1;
    bases.baseSpd =  2;
    bases.baseDef =  5;
    bases.baseRes =  5; // Mages get Res
    bases.growthHP  =  75;
    bases.growthStr =  35;
    bases.growthSkl =  40;
    bases.growthSpd =  32;
    bases.growthLck =  15;
    bases.growthDef =  15;
    bases.growthRes =  35; // to match Pegasus Knight
    var static_stats = bases.build(14);
    b.stats(static_stats);
    WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats) };
    b.weapons(new ArrayList<>(Arrays.asList(weapons)));

    b.name("Cloister Boat pack");
    GBAFEUnitModel model = b.build();
    model.baseActions.add(0, UnitActionFactory.LAUNCH);
    return model;
  }
  public static GBAFEUnitModel CloisterBoat()
  {
    var b = SiegeBoatPack().toBuilder();
    b.name("Cloister Boat");
    b.baseMovePower(5);

    GBAFEUnitModel model = b.build();
    //                                        attack
    model.baseActions.add(2, new GBAFEActions.HealStaffFactory("PHYSIC (7)", 7, 10));
    return model;
  }

}
