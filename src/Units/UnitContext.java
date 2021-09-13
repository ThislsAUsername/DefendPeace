package Units;

import java.util.ArrayList;
import java.util.List;

import CommandingOfficers.Commander;
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
  public XYCoord coord;

  public int maxHP;
  public int attackPower;
  public int defensePower;

  public Environment env;
  public Integer terrainStars = null;

  public WeaponModel weapon;

  /** Only set by the clone constructor */
  public List<UnitModifier> mods = new ArrayList<UnitModifier>();

  public UnitContext(GameMap map, Unit u, WeaponModel w, int x, int y)
  {
    this(u);
    weapon = w;
    coord = new XYCoord(x, y);
    setEnvironment(map.getEnvironment(coord));
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
  public UnitContext(Commander co, UnitModel um)
  {
    super(co, um);
    initModel();
  }
  public UnitContext(UnitContext other)
  {
    super(other);
    unit = other.unit;
    coord = other.coord;
    maxHP = other.maxHP;
    attackPower = other.attackPower;
    defensePower = other.defensePower;
    env = other.env;
    terrainStars = other.terrainStars;
    weapon = other.weapon;
    mods.addAll(other.mods);
  }
  public void initModel()
  {
    maxHP = model.maxHP;
    attackPower = model.getDamageRatio();
    defensePower = model.getDefenseRatio();
  }

  public void setEnvironment(Environment input)
  {
    env = input;
    terrainStars = 0;
    // Air units shouldn't get terrain defense
    if( !model.isAirUnit() )
      terrainStars = env.terrainType.getDefLevel();
  }
}
