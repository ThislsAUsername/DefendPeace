package Units;

import java.util.ArrayList;
import java.util.List;

import CommandingOfficers.Commander;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.UnitMods.UnitModifier;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Units.MoveTypes.MoveType;

/**
 * A basic struct for details relevant to calculation of unit activities.
 * <p>Intended to be ephemeral, and thus free to modify (potentially destructively)
 */
public class UnitContext extends UnitState
{
  private static final long serialVersionUID = 1L;

  // Groups are set together
  public Unit unit;

  public GameMap map;

  public GamePath path;
  public XYCoord coord;

  public int attackPower;
  public int defensePower;
  public int capturePower; // in percent
  public int cargoCapacity;

  public MoveType moveType;
  public int fuelBurnIdle;
  public int movePower;
  public int visionRange;
  public boolean visionPierces;

  public int costBase;
  public int costRatio;
  public int costShift;

  public Environment env;
  public int terrainStars = 0;

  public WeaponModel weapon;
  public int rangeMin = -1, rangeMax = -1;

  public final List<UnitActionFactory> actionTypes = new ArrayList<UnitActionFactory>();

  public final List<UnitModifier> mods = new ArrayList<>();

  /**
   * Builds a UnitContext grounded to a specific Unit on its current tile, with map awareness.
   */
  public UnitContext(GameMap map, Unit u)
  {
    this(map, u, null);
  }
  /**
   * Builds a UnitContext grounded to a specific Unit on its current tile, with map awareness.
   * <p>Also allows selecting a specific weapon.
   */
  public UnitContext(GameMap map, Unit u, WeaponModel w)
  {
    this(map, u, w, null, new XYCoord(u));
  }
  /**
   * Builds a UnitContext on specific Unit on an arbitrary tile, with map awareness.
   * @param w Optional; pre-selects the weapon the unit will use.
   * @param path The path the unit is considering taking to get to coord. Optional; expected to be null or lead to coord.
   * @param coord Where the unit wants to consider from. Technically optional; expected to be valid if path != null.
   */
  public UnitContext(GameMap map, Unit u, WeaponModel w, GamePath path, XYCoord coord)
  {
    this(u);
    this.map = map;
    this.path = path;
    this.coord = coord;
    setEnvironment(map.getEnvironment(coord));
    setWeapon(w);
  }
  /**
   * Builds a UnitContext around a specific Unit, with the minimal knowledge we can scrape from Unit state alone
   */
  public UnitContext(Unit u)
  {
    super(u);
    unit = u;
    coord = new XYCoord(u);
    heldUnits.addAll(u.heldUnits);
    mods.addAll(u.getModifiers());
    initModel();
  }
  /**
   * Builds a UnitContext not tied to any specific unit.
   * <p>Both parameters had better be non-null.
   */
  public UnitContext(Commander co, UnitModel um)
  {
    super(co, um);
    mods.addAll(um.getModifiers());
    mods.addAll(co.getModifiers());
    initModel();
  }
  /**
   * Simple copy constructor.
   */
  public UnitContext(UnitContext other)
  {
    super(other);
    unit = other.unit;
    map  = other.map;
    path = other.path;
    coord = other.coord;

    attackPower = other.attackPower;
    defensePower = other.defensePower;
    capturePower = other.capturePower;
    cargoCapacity = other.cargoCapacity;

    moveType = other.moveType;
    fuelBurnIdle = other.fuelBurnIdle;
    movePower = other.movePower;
    visionRange = other.visionRange;
    visionPierces = other.visionPierces;

    costBase  = other.costBase;
    costRatio = other.costRatio;
    costShift = other.costShift;

    env = other.env;
    terrainStars = other.terrainStars;

    weapon = other.weapon;
    rangeMin = other.rangeMin;
    rangeMax = other.rangeMax;
    actionTypes.addAll(other.actionTypes);
    mods.addAll(other.mods);
  }
  public void initModel()
  {
    attackPower = UnitModel.DEFAULT_STAT_RATIO;
    defensePower = UnitModel.DEFAULT_STAT_RATIO;
    capturePower = UnitModel.DEFAULT_STAT_RATIO;
    cargoCapacity = model.baseCargoCapacity;

    moveType = model.baseMoveType; // This should be safe to not deep-copy until we know we want to change it
    fuelBurnIdle = model.fuelBurnIdle;
    movePower = model.baseMovePower;
    visionRange = model.visionRange;
    visionPierces = model.visionPierces;

    costBase = model.costBase;
    costRatio = UnitModel.DEFAULT_STAT_RATIO;
    costShift = 0;
    actionTypes.addAll(model.baseActions);
  }

  public void setCoord(XYCoord coord)
  {
    this.coord = coord;
    if( null != map && null != coord )
      setEnvironment(map.getEnvironment(coord));
  }

  public void setPath(GamePath pPath)
  {
    path = pPath;
    coord = path.getEndCoord();
    if( null != map && null != coord )
      setEnvironment(map.getEnvironment(coord));
  }

  public void setEnvironment(Environment input)
  {
    env = input;
    // Air units shouldn't get terrain defense
    if( null != env && !model.isAirUnit() )
      terrainStars = env.terrainType.getDefLevel();
  }

  public void setWeapon(WeaponModel w)
  {
    weapon = w;
    if( null != w )
    {
      rangeMin = w.rangeMin;
      rangeMax = w.rangeMax;
      if( env != null && env.weatherType == Weathers.SANDSTORM && !CO.immuneToSand )
        --rangeMax;
      for( UnitModifier mod : mods )
        mod.modifyAttackRange(this);
      if( rangeMax < rangeMin )
        rangeMax = rangeMin;
    }
    else
    {
      rangeMin = -1;
      rangeMax = -1;
    }
  }

  public int getCostTotal()
  {
    int scaledCost = (costBase + costShift) * costRatio / UnitModel.DEFAULT_STAT_RATIO;
    // Trim off the ones' place for that extra bit of cart accuracy
    scaledCost -= scaledCost % 10;
    return scaledCost;
  }

  /**
   * Calculates the true move power, updating the field as well
   * @return The real move power
   */
  public int calculateMovePower()
  {
    this.movePower = model.baseMovePower;
    if( env != null && env.weatherType == Weathers.SLEET && !CO.immuneToCold )
      --movePower;
    for( UnitModifier mod : mods )
      mod.modifyMovePower(this);
    if( movePower < 0 )
      movePower = 0;
    return movePower;
  }

  /**
   * @return The new capture progress I would make
   */
  public int calculateCapturePower()
  {
    this.capturePower = UnitModel.DEFAULT_STAT_RATIO;
    for( UnitModifier mod : mods )
      mod.modifyCapturePower(this);

    int captureProgress = getHP() * capturePower / UnitModel.DEFAULT_STAT_RATIO;
    if( captureProgress < 0 )
      captureProgress = 0;
    return captureProgress;
  }

  public int calculateFuelBurnIdle(Environment env)
  {
    this.env = env;
    fuelBurnIdle = model.fuelBurnIdle;
    for( UnitModifier mod : mods )
      mod.modifyIdleFuelBurn(this);

    // Don't burn fuel while in port
    if( model.healableHabs.contains(env.terrainType) )
      fuelBurnIdle = Math.min(0, fuelBurnIdle);
    return fuelBurnIdle;
  }

  public int calculateVision()
  {
    visionRange = model.visionRange;
    visionPierces = model.visionPierces;
    if( null != env )
    {
      // if it's a surface unit, give it the boost the terrain would provide, so long as it's not adjacent-only vision
      if( model.isSurfaceUnit() )
        visionRange += env.terrainType.getVisionBoost();
      if( env.weatherType == Weathers.RAIN && !CO.immuneToClouds )
        --visionRange;
      if( env.weatherType == Weathers.SMOKE && !CO.immuneToClouds )
      {
        // SMOKE sets vision to 1 (before buffs but after mountains) in DoR, but Drake's version in DS only subtracts 1 from vision.
        if( null != unit && unit.CO.gameRules.fogMode.dorMode )
          visionRange = 1;
        else // Default to Trilogy logic, since "has most of normal vision" will stick out more vs "vision = 1" than vice versa.
          --visionRange;
      }
    }
    for( UnitModifier mod : mods )
      mod.modifyVision(this);
    if( visionRange < 1 )
      visionRange = 1;
    return visionRange;
  }

  /**
   * Calculates the available action types, updating the field as well
   */
  public List<UnitActionFactory> calculateActionTypes()
  {
    actionTypes.clear();
    actionTypes.addAll(model.baseActions);
    for( UnitModifier mod : mods )
      mod.modifyActionList(this);
    return actionTypes;
  }

  /**
   * Calculates the real movetype, updating the field as well
   */
  public MoveType calculateMoveType()
  {
    moveType = model.baseMoveType.clone();
    for( UnitModifier mod : mods )
      mod.modifyMoveType(this);
    return moveType;
  }

  public int calculateCargoCapacity()
  {
    cargoCapacity = model.baseCargoCapacity;
    for( UnitModifier mod : mods )
      mod.modifyCargoCapacity(this);
    return cargoCapacity;
  }


  /**
   * Assign weapon to the available one that can inflict the most damage against the chosen target
   */
  public void chooseWeapon(ITargetable targetType, int range)
  {
    boolean afterMoving = false;
    if( null != path )
      afterMoving = path.getPathLength() > 1;
    else if( null != unit && null != coord )
      afterMoving = unit.x != coord.x || unit.y != coord.y;

    chooseWeapon(targetType, range, afterMoving);
  }
  public void chooseWeapon(ITargetable targetType, int range, boolean afterMoving)
  {
    // if we have no weapons, we can't hurt things
    if( model.weapons == null )
      return;

    WeaponModel chosenWeapon = null;
    double maxDamage = 0;
    for( WeaponModel w : model.weapons )
    {
      if( !w.loaded(this) ) continue; // Can't shoot with no bullets.

      // If the weapon isn't mobile, we cannot fire if we moved.
      if( afterMoving && !w.canFireAfterMoving )
      {
        continue;
      }

      UnitContext uc = new UnitContext(this);
      uc.setWeapon(w);

      if( uc.rangeMax < range || uc.rangeMin > range )
      {
        // Can only hit things inside our range
        continue;
      }

      double currentDamage = w.getDamage(targetType);
      if( currentDamage > maxDamage )
      {
        chosenWeapon = w;
        maxDamage = currentDamage;
      }
    }

    setWeapon(chosenWeapon);
  }

  @Override
  public String toString()
  {
    return "Context for "+model.name+" at "+coord+", using weapon "+weapon;
  }
}
