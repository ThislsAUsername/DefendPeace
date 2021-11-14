package Engine.UnitMods;

import java.util.Map;
import CommandingOfficers.Commander;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Units.Unit;

public class BuildCountsTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  private CountTracker<Commander, XYCoord> buildCounts = new CountTracker<>();

  /** Caller shouldn't modify this return value */
  public Map<XYCoord, Integer> getCountFor(Commander co)
  {
    return buildCounts.getCountFor(co);
  }
  public int getCountFor(Commander co, XYCoord coord)
  {
    return buildCounts.getCountFor(co, coord);
  }

  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    XYCoord buildCoords = new XYCoord(unit.x, unit.y);
    buildCounts.incrementCount(unit.CO, buildCoords);

    return null;
  }
  @Override
  public GameEventQueue receiveTurnInitEvent(Commander co, int turn)
  {
    buildCounts.resetCountFor(co);

    return null;
  }
}
