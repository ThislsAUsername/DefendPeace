package Units;

import java.util.ArrayList;

import Engine.GameInstance;
import Engine.UnitActionFactory;
import Engine.UnitActionLifecycles.TransformLifecycle;
import Terrain.TerrainType;
import Units.MoveTypes.*;

public class GBAFEUnits extends UnitModelScheme
{
  private static final long serialVersionUID = 1L;

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
    GameReadyModels feModels = new GameReadyModels();

    ArrayList<UnitModel> factoryModels = new ArrayList<>();
    ArrayList<UnitModel> shipModels = new ArrayList<>();
    ArrayList<UnitModel> airportModels = new ArrayList<>();
    ArrayList<UnitModel> extras = new ArrayList<>();

    // Define everything we can build from a Factory.
    factoryModels.add(new Soldier());
    factoryModels.add(new Myrmidon());
    factoryModels.add(new Thief());
    factoryModels.add(new Axeman());
    factoryModels.add(new Mercenary());
    Priest priest = new Priest();
    factoryModels.add(priest);
    factoryModels.add(new Troubadour());
    factoryModels.add(new Mage());
    factoryModels.add(new Shaman());
    Monk monk = new Monk();
    factoryModels.add(monk);
    factoryModels.add(new Nomad());
    factoryModels.add(new Archer());
    factoryModels.add(new ArmorKnight());
    factoryModels.add(new Cavalier());
    factoryModels.add(new GreatKnight());
    factoryModels.add(new Bard());

    // Inscribe those war machines obtainable from an Airport.
    airportModels.add(new PegKnight());
    airportModels.add(new WyvernRider());
    airportModels.add(new WyvernKnight());
    airportModels.add(new Summoner());

    // Record those units we can get from a Seaport.
    shipModels.add(new Pirate());
    shipModels.add(new Fleet());
    shipModels.add(new SiegeBoat());
    shipModels.add(new CloisterBoat());

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
      ((GBAFEUnitModel) feModels.unitModels.get(i)).addVariants(feModels.unitModels, i);

    // Do this after adding all the models, since we don't want two Bishop entries
    priest.promotesTo = monk.promotesTo;
    priest.baseActions.add(new GBAFEActions.PromotionFactory(priest.promotesTo));

    return feModels;
  }

  public void registerStateTrackers(GameInstance gi)
  {
    super.registerStateTrackers(gi);

//    StateTracker.instance(gi, UnitTurnPositionTracker.class);
    // TODO: Experience tracker (kills/capture)
    // TODO: Phantom tracker?
//    UnitResurrectionTracker rezzer = StateTracker.instance(gi, UnitResurrectionTracker.class);
//    // Populate resurrection pairs
//    GameReadyModels grms = gi.rules.unitModelScheme.getGameReadyModels();
//    for( UnitModel um : grms.unitModels )
//    {
//      KaijuWarsUnitModel umCast = (KaijuWarsUnitModel) um;
//      if( null == umCast.resurrectsAs )
//        continue;
//      rezzer.resurrectionTypeMap.put(um, umCast.resurrectsAs);
//    }
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
      stats.level = levels+1;
      if( promoted )
        levels += 9; // 7/8 Easy use 9; all others use 19, but 19 is nuts
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
      stats.HP    = baseHP  + growthHP *levels/100;
      stats.Str   = baseStr + growthStr*levels/100;
      stats.Skl   = baseSkl + growthSkl*levels/100;
      stats.Spd   = baseSpd + growthSpd*levels/100;
      stats.Lck   = baseLck + growthLck*levels/100;
      stats.Def   = baseDef + growthDef*levels/100;
      stats.Res   = baseRes + growthRes*levels/100;
      return stats;
    }
  }

  public static class GBAFEStats
  {
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

  public static class GBAFEUnitModel extends UnitModel
  {
    private static final long serialVersionUID = 1L;
    public final GBAFEStats stats;
    public int baseXP = 0;
    public UnitModel promotesTo = null;

    public boolean isArmor   = false;
    public boolean isHorse   = false;

    public GBAFEUnitModel(String pName, GBAFEStats stats, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, UnitActionFactory[] actions, WeaponModel[] WEAPONS, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, WEAPONS, starValue);
      this.stats = stats;
      baseXP = stats.level*100;
      fuelBurnPerTile = 0;
      needsMaterials = false;
      addUnitModifier(new GBAFEWeapons.GBAFEFightMod());
    }
    public GBAFEUnitModel(String pName, GBAFEStats stats, long pRole, int cost, int pAmmoMax, int pFuelMax, int pIdleFuelBurn, int pVision,
        int pMovePower, MoveType pPropulsion, ArrayList<UnitActionFactory> actions, ArrayList<WeaponModel> WEAPONS, double starValue)
    {
      super(pName, pRole, cost, pAmmoMax, pFuelMax, pIdleFuelBurn, pVision, pMovePower, pPropulsion, actions, WEAPONS, starValue);
      this.stats = stats;
      baseXP = stats.level*100;
      fuelBurnPerTile = 0;
      needsMaterials = false;
      addUnitModifier(new GBAFEWeapons.GBAFEFightMod());
    }

    @Override
    public UnitModel clone()
    {
      // Create a new model with the given attributes.
      GBAFEUnitModel newModel = new GBAFEUnitModel(name, stats, role, costBase, maxAmmo, maxFuel, fuelBurnIdle, visionRange, baseMovePower,
          baseMoveType.clone(), baseActions, weapons, abilityPowerValue);

      newModel.copyValues(this);
      return newModel;
    }
    public void copyValues(GBAFEUnitModel other)
    {
      super.copyValues(other);
      baseXP     = other.baseXP;
      promotesTo = other.promotesTo;
    }

    @Override
    public double getDamageRedirect(WeaponModel wm)
    {
      return wm.getDamage(this);
    }

    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( null != this.promotesTo )
      {
        unitList.add(yourIndex+1, this.promotesTo);
        this.baseActions.add(new GBAFEActions.PromotionFactory(this.promotesTo));
      }
    }
  }

  public abstract static class FootUnit extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TROOP | LAND;

    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = VISION_NORMAL;

    private static final MoveType moveType = foot;
    private static final UnitActionFactory[] actions = UnitActionFactory.FOOTSOLDIER_ACTIONS;

    public FootUnit(String name, GBAFEStats stats, int cost, int move, WeaponModel[] weapons, double starVal)
    {
      super(name, stats, ROLE, cost, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, move,
          moveType, actions, weapons, starVal);
    }
  }
  public abstract static class HorseUnit extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = TANK | LAND;

    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = VISION_NORMAL;

    private static final MoveType moveType = hoof;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;

    public HorseUnit(String name, GBAFEStats stats, int cost, int move, WeaponModel[] weapons, double starVal)
    {
      super(name, stats, ROLE, cost, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, move,
          moveType, actions, weapons, starVal);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
      isHorse = true;
    }
  }
  public abstract static class FlierUnit extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = HOVER | AIR_LOW;

    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = VISION_NORMAL;

    private static final MoveType moveType = wing;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;

    public FlierUnit(String name, GBAFEStats stats, int cost, int move, WeaponModel[] weapons, double starVal)
    {
      super(name, stats, ROLE, cost, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, move,
          moveType, actions, weapons, starVal);
      baseCargoCapacity = 1;
      carryableMask = TROOP;
    }
  }

  private static final MoveType wing       = new FEMoveTypes.FEFlight();
  private static final MoveType wheel      = new FEMoveTypes.FEWheel();
  private static final MoveType boat       = new FEMoveTypes.FEBoat();
  private static final MoveType foot       = new FEMoveTypes.FEFoot();
  private static final MoveType footAxe    = new FEMoveTypes.FEFootAxe();
  private static final MoveType footMage   = new FEMoveTypes.FEFootMage();
  private static final MoveType footArmor  = new FEMoveTypes.FEFootArmor();
  private static final MoveType footPirate = new FEMoveTypes.FEFootPirate();
  private static final MoveType hoof       = new FEMoveTypes.FEHoof();
  private static final MoveType hoofPromo  = new FEMoveTypes.FEHoofPromoted();
  private static final MoveType hoofN      = new FEMoveTypes.FEHoofNomad();
  private static final MoveType hoofNPromo = new FEMoveTypes.FEHoofNomadPromoted();
  private static final int MAX_FUEL = 99;
  private static final int IDLE_FUEL_BURN = 0;
  private static final int VISION_NORMAL = 3;
  private static final int VISION_THIEF  = 8;

  public static class Soldier extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 500;
    private static final double STAR_VALUE = 0.2;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats) };

    public Soldier()
    {
      super("Soldier", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Halberdier();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(0);
    }
  }
  public static class Halberdier extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 10000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6; // PoR standardizes on 7; might be amusing

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats), new GBAFEWeapons.Javelin(static_stats) };

    public Halberdier()
    {
      super("Halberdier", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = null;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel javelineer = new Halberdier();
        javelineer.weapons.remove(0);
        javelineer.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex+1, javelineer);
      }
    }
  }

  public static class Myrmidon extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 3000;
    private static final double STAR_VALUE = 0.6;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronSword(static_stats) };

    public Myrmidon()
    {
      super("Myrmidon", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Swordmaster();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(2);
    }
  }
  public static class Swordmaster extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 13000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.KillingEdge(static_stats) };

    public Swordmaster()
    {
      super("Swordmaster", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }
  }

  public static class Thief extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 3000;
    private static final double STAR_VALUE = 0.6;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronSword(static_stats) };

    public Thief()
    {
      super("Thief", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Assassin();
      visionRange = VISION_THIEF;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(6);
    }
  }
  public static class Assassin extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 13000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.KillingEdge(static_stats) };

    public Assassin()
    {
      super("Assassin", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      visionRange = VISION_THIEF;
    }
    private static GBAFEStats buildStats()
    {
      ClassStatsBuilder bases = new ClassStatsBuilder();
      bases.lethality = true;
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
      return bases.build(true, 19);
    }
  }

  // "Fighter" means something different here...
  public static class Axeman extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 4000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Hammer(static_stats) };

    public Axeman()
    {
      super("Axeman", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Warrior();
      baseMoveType = footAxe;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(6);
    }
  }
  public static class Warrior extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Hammer(static_stats), new GBAFEWeapons.IronBow(static_stats) };

    public Warrior()
    {
      super("Warrior", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = footAxe;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new Warrior();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);
      }
    }
  }

  public static class Mercenary extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 6000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronSword(static_stats) };

    public Mercenary()
    {
      super("Mercenary", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Hero();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }
  }
  public static class Hero extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronHero(static_stats) };

    public Hero()
    {
      super("Hero", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }
  }

  public static class ArmorKnight extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 8000;
    private static final double STAR_VALUE = 1.2;
    private static final int MOVE_POWER = 4;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Horseslayer(static_stats) };

    public ArmorKnight()
    {
      super("Armor Knight", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new General();
      isArmor = true;
      baseMoveType = footArmor;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }
  }
  public static class General extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.GeneralWeapons(static_stats), new GBAFEWeapons.Javelina(static_stats) };

    public General()
    {
      super("General", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      isArmor = true;
      baseMoveType = footArmor;
    }
    private static GBAFEStats buildStats()
    {
      ClassStatsBuilder bases = new ClassStatsBuilder();
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
      return bases.build(true, 2);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new General();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);
      }
    }
  }

  public static class GreatKnight extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 22000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronTriangle(static_stats) };

    public GreatKnight()
    {
      super("Great Knight", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = hoofPromo;
      isArmor = true;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 0);
    }
  }

  public static class Nomad extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 1.2;
    private static final int MOVE_POWER = 7;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronBow(static_stats) };

    public Nomad()
    {
      super("Nomad", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new NomadTrooper();
      baseMoveType = hoofN;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(2);
    }
  }
  public static class NomadTrooper extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 10000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 8;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.KillingEdge(static_stats), new GBAFEWeapons.KillerBow(static_stats) };

    public NomadTrooper()
    {
      super("Nomad Trooper", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = hoofNPromo;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 0); // The idea is "promo gains and killer weapons only"
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new NomadTrooper();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);
      }
    }
  }

  public static class ArcherInBallista extends FootUnit
  {
    private static final long serialVersionUID = 1L;
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Ballista(Archer.static_stats) };

    public ArcherInBallista()
    {
      super("Ballista", Archer.static_stats, Archer.UNIT_COST, Archer.MOVE_POWER, weapons, Archer.STAR_VALUE);
      baseActions.clear();
      for( UnitActionFactory action : UnitActionFactory.COMBAT_VEHICLE_ACTIONS )
        baseActions.add(action);
      maxAmmo = 5;
      role = TANK | LAND;
      healableHabs.clear(); // No free resupplies for you
      baseMoveType = wheel;
    }
  }
  public static class Archer extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 6000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronBow(static_stats), new GBAFEWeapons.Longbow(static_stats) };

    public Archer()
    {
      super("Archer", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Sniper();
    }
    private static GBAFEStats buildStats()
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
      return bases.build(14); // Wow, this class's stats are bad
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new Archer();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);

        UnitModel ballista = new ArcherInBallista();
        baseActions.add(new GBAFEActions.PromotionFactory(ballista));
        ballista.baseActions.add(new TransformLifecycle.TransformFactory(this, "DISMOUNT"));
        unitList.add(yourIndex + 2, ballista);
      }
    }
  }
  public static class SniperInBallista extends FootUnit
  {
    private static final long serialVersionUID = 1L;
    private static final WeaponModel[] weapons = { new GBAFEWeapons.KillerBallista(Sniper.static_stats) };

    public SniperInBallista()
    {
      super("Killer Ballista", Sniper.static_stats, Sniper.UNIT_COST, Sniper.MOVE_POWER, weapons, Sniper.STAR_VALUE);
      baseActions.clear();
      for( UnitActionFactory action : UnitActionFactory.COMBAT_VEHICLE_ACTIONS )
        baseActions.add(action);
      maxAmmo = 5;
      role = TANK | LAND;
      healableHabs.clear(); // No free resupplies for you
      baseMoveType = wheel;
    }
  }
  public static class Sniper extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.KillerBow(static_stats), new GBAFEWeapons.Longbow(static_stats) };

    public Sniper()
    {
      super("Sniper", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
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
      return bases.build(true, 9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new Sniper();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);

        UnitModel killerBallista = new SniperInBallista();
        baseActions.add(new GBAFEActions.PromotionFactory(killerBallista));
        killerBallista   .baseActions.add(new TransformLifecycle.TransformFactory(this, "DISMOUNT"));
        unitList.add(yourIndex + 2, killerBallista);
      }
    }
  }

  public static class Priest extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 3000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();

    public Priest()
    {
      super("Priest", static_stats, UNIT_COST, MOVE_POWER, new WeaponModel[0], STAR_VALUE);
      // Promotion is set by the caller
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(6);
    }
  }

  public static class Troubadour extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 7;

    private static final GBAFEStats static_stats = buildStats();

    public Troubadour()
    {
      super("Troubadour", static_stats, UNIT_COST, MOVE_POWER, new WeaponModel[0], STAR_VALUE);
      promotesTo = new Valkyrie();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }
  }
  public static class Valkyrie extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 2.0;
    private static final int MOVE_POWER = 8;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats) };

    public Valkyrie()
    {
      super("Valkyrie", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 4);
    }
  }

  public static class Mage extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats) };

    public Mage()
    {
      super("Mage", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Sage();
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(4);
    }
  }
  public static class Sage extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 30000;
    private static final double STAR_VALUE = 3.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats), new GBAFEWeapons.Bolting(static_stats) };

    public Sage()
    {
      super("Sage", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new Sage();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);
      }
    }
  }

  public static class Shaman extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Flux(static_stats) };

    public Shaman()
    {
      super("Shaman", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Druid();
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(6);
    }
  }
  public static class Druid extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.6;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Flux(static_stats), new GBAFEWeapons.Luna(static_stats) };

    public Druid()
    {
      super("Druid", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 14);
    }
  }

  public static class Monk extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 6000;
    private static final double STAR_VALUE = 0.8;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Lightning(static_stats) };

    public Monk()
    {
      super("Monk", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Bishop();
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }
  }
  public static class Bishop extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 13000;
    private static final double STAR_VALUE = 3.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Lightning(static_stats) };

    public Bishop()
    {
      super("Bishop", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = footMage;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }
  }

  public static class Cavalier extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 10000;
    private static final double STAR_VALUE = 1.2;
    private static final int MOVE_POWER = 7;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };

    public Cavalier()
    {
      super("Cavalier", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Paladin();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }
  }
  public static class Paladin extends HorseUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 8;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };

    public Paladin()
    {
      super("Paladin", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = hoofPromo;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }
  }

  public static class Bard extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 13000;
    private static final double STAR_VALUE = 2.0;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();

    public Bard()
    {
      super("Bard", static_stats, UNIT_COST, MOVE_POWER, new WeaponModel[0], STAR_VALUE);
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }
  }

  public static class PegKnight extends FlierUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 4000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 7;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats), new GBAFEWeapons.Javelin(static_stats) };

    public PegKnight()
    {
      super("Pegasus Knight", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new FalcoKnight();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(3);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new PegKnight();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);
      }
    }
  }
  public static class FalcoKnight extends FlierUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.2;
    private static final int MOVE_POWER = 8;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats), new GBAFEWeapons.Javelin(static_stats) };

    public FalcoKnight()
    {
      super("Falco Knight", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = null;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      if( !hidden )
      {
        super.addVariants(unitList, yourIndex);
        UnitModel rangedOnly = new FalcoKnight();
        rangedOnly.weapons.remove(0);
        rangedOnly.hidden = true; // for visual distinctiveness
        unitList.add(yourIndex + 1, rangedOnly);
      }
    }
  }

  public static class WyvernRider extends FlierUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 7000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 7;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronLance(static_stats) };

    public WyvernRider()
    {
      super("Wyvern Rider", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new WyvernLord();
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(6);
    }
  }
  public static class WyvernLord extends FlierUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 12000;
    private static final double STAR_VALUE = 1.2;
    private static final int MOVE_POWER = 8;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };

    public WyvernLord()
    {
      super("Wyvern Lord", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = null;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 6);
    }
  }

  public static class WyvernKnight extends FlierUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 18000;
    private static final double STAR_VALUE = 1.6;
    private static final int MOVE_POWER = 8;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.IronCav(static_stats) };

    public WyvernKnight()
    {
      super("Wyvern Knight", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = null;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 0);
    }
  }

  public static class Summoner extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 22000;
    private static final double STAR_VALUE = 2.6;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Flux(static_stats) };

    public Summoner()
    {
      super("Summoner", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = footMage;
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
  }

  public static class Pirate extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 5000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final WeaponModel[] weapons = { new GBAFEWeapons.HandAxe(static_stats) };

    public Pirate()
    {
      super("Pirate", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      promotesTo = new Berserker();
      baseMoveType = footPirate;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(0);
    }
  }
  public static class Berserker extends FootUnit
  {
    private static final long serialVersionUID = 1L;

    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.0;
    private static final int MOVE_POWER = 6;

    private static final GBAFEStats static_stats = buildStats();
    // TODO: Killer axe?
    private static final WeaponModel[] weapons = { new GBAFEWeapons.HandAxe(static_stats) };

    public Berserker()
    {
      super("Berserker", static_stats, UNIT_COST, MOVE_POWER, weapons, STAR_VALUE);
      baseMoveType = footPirate;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 9);
    }
  }

  public static class FleetPack extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MOVE_POWER = 20;
    private static final UnitActionFactory[] actions = UnitActionFactory.TRANSPORT_ACTIONS;

    public FleetPack()
    {
      super("Fleet pack", Fleet.static_stats, Fleet.ROLE, Fleet.UNIT_COST, Fleet.MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, Fleet.VISION_RANGE, MOVE_POWER,
          Fleet.moveType, actions, new WeaponModel[0], Fleet.STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK | HOVER;
    }
  }
  public static class Fleet extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SHIP | SEA;

    private static final int UNIT_COST = 16000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_AMMO = 5;
    private static final int VISION_RANGE = VISION_NORMAL;
    private static final int MOVE_POWER = 3;

    private static final GBAFEStats static_stats = buildStats();
    private static final MoveType moveType = boat;
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Ballista(static_stats) };
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;

    public Fleet()
    {
      super("Fleet", static_stats, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK | HOVER;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      super.addVariants(unitList, yourIndex);
      UnitModel fleetPack = new FleetPack();
      baseActions.add(new TransformLifecycle.TransformFactory(fleetPack, "PACK"));
      fleetPack.baseActions.add(new TransformLifecycle.TransformFactory(this, "UNPACK"));
      unitList.add(yourIndex + 1, fleetPack);
    }
  }

  public static class SiegeBoatPack extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MOVE_POWER = 20;
    private static final UnitActionFactory[] actions = UnitActionFactory.TRANSPORT_ACTIONS;

    public SiegeBoatPack()
    {
      super("Siege Boat pack", SiegeBoat.static_stats, SiegeBoat.ROLE, SiegeBoat.UNIT_COST, SiegeBoat.MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, SiegeBoat.VISION_RANGE, MOVE_POWER,
          SiegeBoat.moveType, actions, new WeaponModel[0], SiegeBoat.STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK | HOVER;
    }
  }
  public static class SiegeBoat extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SHIP | SEA;

    private static final int UNIT_COST = 24000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_AMMO = 5;
    private static final int VISION_RANGE = VISION_NORMAL;
    private static final int MOVE_POWER = 3;

    private static final GBAFEStats static_stats = buildStats();
    private static final MoveType moveType = boat;
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Trebuchet(static_stats) };
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;

    public SiegeBoat()
    {
      super("Siege Boat", static_stats, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK | HOVER;
    }
    private static GBAFEStats buildStats()
    {
      ClassStatsBuilder bases = new ClassStatsBuilder();
      bases.baseHP  = 19;
      bases.baseStr =  1;
      bases.baseSkl =  1;
      bases.baseSpd =  2;
      bases.baseDef = 11; // Armor boat gooo
      bases.baseRes =  0;
      bases.growthHP  =  75;
      bases.growthStr =  35;
      bases.growthSkl =  40;
      bases.growthSpd =  32;
      bases.growthLck =  15;
      bases.growthDef =  15;
      bases.growthRes =  15;
      return bases.build(true, 9);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      super.addVariants(unitList, yourIndex);
      UnitModel siegeBoatPack = new SiegeBoatPack();
      baseActions.add(new TransformLifecycle.TransformFactory(siegeBoatPack, "PACK"));
      siegeBoatPack.baseActions.add(new TransformLifecycle.TransformFactory(this, "UNPACK"));
      unitList.add(yourIndex + 1, siegeBoatPack);
    }
  }

  public static class CloisterBoatPack extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MOVE_POWER = 20;
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;

    public CloisterBoatPack()
    {
      super("Cloister Boat pack", CloisterBoat.static_stats, CloisterBoat.ROLE, CloisterBoat.UNIT_COST, CloisterBoat.MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, CloisterBoat.VISION_RANGE, MOVE_POWER,
          CloisterBoat.moveType, actions, CloisterBoat.weapons, CloisterBoat.STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK | HOVER;
    }
  }
  public static class CloisterBoat extends GBAFEUnitModel
  {
    private static final long serialVersionUID = 1L;
    private static final long ROLE = SHIP | SEA;

    private static final int UNIT_COST = 24000;
    private static final double STAR_VALUE = 1.8;
    private static final int MAX_AMMO = -1;
    private static final int VISION_RANGE = VISION_NORMAL;
    private static final int MOVE_POWER = 5;

    private static final GBAFEStats static_stats = buildStats();
    private static final MoveType moveType = boat;
    private static final WeaponModel[] weapons = { new GBAFEWeapons.Fire(static_stats) };
    private static final UnitActionFactory[] actions = UnitActionFactory.COMBAT_TRANSPORT_ACTIONS;

    public CloisterBoat()
    {
      super("Cloister Boat", static_stats, ROLE, UNIT_COST, MAX_AMMO, MAX_FUEL, IDLE_FUEL_BURN, VISION_RANGE, MOVE_POWER,
          moveType, actions, weapons, STAR_VALUE);
      baseCargoCapacity = 2;
      carryableMask = TROOP | TANK | HOVER;
    }
    private static GBAFEStats buildStats()
    {
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
      return bases.build(true, 14);
    }

    @Override
    public void addVariants(ArrayList<UnitModel> unitList, int yourIndex)
    {
      super.addVariants(unitList, yourIndex);
      UnitModel cloisterBoatPack = new CloisterBoatPack();
      baseActions.add(new TransformLifecycle.TransformFactory(cloisterBoatPack, "PACK"));
      cloisterBoatPack.baseActions.add(new TransformLifecycle.TransformFactory(this, "UNPACK"));
      unitList.add(yourIndex + 1, cloisterBoatPack);
    }
  }

}
