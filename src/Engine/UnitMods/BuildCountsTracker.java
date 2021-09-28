package Engine.UnitMods;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Units.Unit;

public class BuildCountsTracker extends StateTracker<BuildCountsTracker>
{
  private static final long serialVersionUID = 1L;

  protected BuildCountsTracker(Class<BuildCountsTracker> key, GameInstance gi)
  {
    super(key, gi);
  }
  @Override
  protected BuildCountsTracker item()
  {
    return this;
  }

  private CountTracker<Commander, XYCoord> buildCounts = new CountTracker<>();

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
