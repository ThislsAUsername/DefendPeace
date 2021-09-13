package Units;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapLocation;

/**
 * A basic struct for details relevant to calculation of unit activities.
 * <p>Intended to be ephemeral, and thus free to modify (potentially destructively)
 */
public class UnitContext extends UnitState
{
  private static final long serialVersionUID = 1L;

  public Unit unit;
  public XYCoord coord;
  public MapLocation loc;

  // Combat stuff
  public int maxHP;
  public int COstr;
  public int COdef;

  public UnitContext(Unit u)
  {
    this(u.CO, u.model);
    unit = u;
    coord = new XYCoord(u.x, u.y);
    heldUnits.addAll(u.heldUnits);
  }
  public UnitContext(Commander co, UnitModel um)
  {
    super(co, um);
    maxHP = um.maxHP;
    COstr = um.getDamageRatio();
    COdef = um.getDefenseRatio();
  }
}
