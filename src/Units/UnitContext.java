package Units;

import java.util.ArrayList;
import java.util.List;

import CommandingOfficers.Commander;
import Engine.GamePath;
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

  public final List<UnitModifier> mods = new ArrayList<>();

  public UnitContext(GameMap map, Unit u, WeaponModel w, GamePath path, XYCoord coord)
  {
    this(u);
    this.map = map;
    this.path = path;
    this.coord = coord;
    setEnvironment(map.getEnvironment(coord));
    setWeapon(w);
  }
  public UnitContext(GameMap map, Unit u, WeaponModel w, int x, int y)
  {
    this(u);
    this.map = map;
    coord = new XYCoord(x, y);
    setEnvironment(map.getEnvironment(coord));
    setWeapon(w);
  }
  public UnitContext(Unit u)
  {
    super(u);
    unit = u;
    coord = new XYCoord(u.x, u.y);
    heldUnits.addAll(u.heldUnits);
    mods.addAll(u.getModifiers());
    initModel();
  }
  // Note: the Commander argument is here as a placeholder for Army
  // The plan is for Unit to have an Army reference, and UnitModel to have the Commander reference
  public UnitContext(Commander co, UnitModel um)
  {
    super(co, um);
    initModel();
  }
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
    maxHP = model.maxHP;
    attackPower = model.getDamageRatio();
    defensePower = model.getDefenseRatio();
    movePower = model.movePower;
    costBase = model.costBase;
    costMultiplier = model.costMultiplier;
    costShift = model.costShift;
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
      // Build a scratch UnitContext because we don't trust UnitMods with our state
      UnitContext uc = new UnitContext(this);
      for( UnitModifier mod : mods )
        mod.modifyAttackRange(uc);
      rangeMin = uc.rangeMin;
      rangeMax = uc.rangeMax;
      if( rangeMax < rangeMin )
        rangeMax = rangeMin;
    }
    else
    {
      rangeMin = -1;
      rangeMax = -1;
    }
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
    return model.name;
  }
}
