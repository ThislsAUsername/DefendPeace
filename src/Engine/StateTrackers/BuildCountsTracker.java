package Engine.StateTrackers;

import java.util.Map;
import CommandingOfficers.Commander;
import Engine.Army;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Units.Unit;

public class BuildCountsTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  private CountManager<Commander, XYCoord> buildCounts = new CountManager<>();

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
  public GameEventQueue receiveTurnEndEvent(Army army, int turn)
  {
    for( Commander co : army.cos )
      buildCounts.resetCountFor(co);

    return null;
  }
}
