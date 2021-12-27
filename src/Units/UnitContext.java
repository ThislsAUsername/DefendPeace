package Units;

import java.util.ArrayList;
import java.util.List;

import CommandingOfficers.Commander;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Engine.UnitMods.UnitModifier;
import Terrain.Environment;
import Terrain.GameMap;

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

  public int maxHP;
  public int attackPower;
  public int defensePower;
  public int movePower;
  public int costBase;
  public double costMultiplier;
  public int costShift;

  public Environment env;
  public int terrainStars = 0;

  public WeaponModel weapon;
  public int rangeMin = -1, rangeMax = -1;

  public final List<UnitActionFactory> possibleActions = new ArrayList<UnitActionFactory>();

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
    initModel();
  }
  /**
   * Simple copy constructor.
   */
  public UnitContext(UnitContext other)
  {
    super(other);
    unit = other.unit;
    path = other.path;
    coord = other.coord;
    maxHP = other.maxHP;
    attackPower = other.attackPower;
    defensePower = other.defensePower;
    env = other.env;
    terrainStars = other.terrainStars;
    weapon = other.weapon;
    rangeMin = other.rangeMin;
    rangeMax = other.rangeMax;
    mods.addAll(other.mods);
  }
  public void initModel()
  {
    maxHP = UnitModel.MAXIMUM_HP;
    attackPower = UnitModel.DEFAULT_STAT_RATIO;
    defensePower = UnitModel.DEFAULT_STAT_RATIO;
    movePower = model.baseMovePower;
    costBase = model.costBase;
    costMultiplier = 1.0;
    costShift = 0;
    possibleActions.addAll(model.baseActions);
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
    return (int) (costBase*costMultiplier)+costShift;
  }

  /**
   * Calculates the true move power, updating the field as well
   * @return The real move power
   */
  public int calculateMovePower()
  {
    this.movePower = model.baseMovePower;
    for( UnitModifier mod : mods )
      mod.modifyMovePower(this);
    return movePower;
  }

  /**
   * Calculates the available action types, updating the field as well
   */
  public List<UnitActionFactory> calculatePossibleActions()
  {
    possibleActions.clear();
    possibleActions.addAll(model.baseActions);
    for( UnitModifier mod : mods )
      mod.modifyActionList(this);
    return possibleActions;
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
      afterMoving = unit.x != coord.xCoord || unit.y != coord.yCoord;

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
