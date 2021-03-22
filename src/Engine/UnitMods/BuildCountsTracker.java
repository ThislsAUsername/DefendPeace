package Engine.UnitMods;

import java.util.HashMap;

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

  private HashMap<Commander, HashMap<XYCoord, Integer>> buildCounts = new HashMap<>();

  public HashMap<XYCoord, Integer> getCountFor(Commander co)
  {
    if( !buildCounts.containsKey(co) )
      buildCounts.put(co, new HashMap<XYCoord, Integer>());
    return buildCounts.get(co);
  }
  public int getCountFor(Commander co, XYCoord coord)
  {
    HashMap<XYCoord, Integer> coCounts = getCountFor(co);
    if( !coCounts.containsKey(coord) )
      coCounts.put(coord, 0);
    return coCounts.get(coord);
  }
  public void incrementCount(Commander co, XYCoord coord)
  {
    HashMap<XYCoord, Integer> coCounts = getCountFor(co);
    int prevCount = getCountFor(co, coord);
    coCounts.put(coord, prevCount + 1);
  }
  public void resetCountFor(Commander co)
  {
    buildCounts.put(co, new HashMap<XYCoord, Integer>());
  }

  @Override
  public GameEventQueue receiveCreateUnitEvent(Unit unit)
  {
    XYCoord buildCoords = new XYCoord(unit.x, unit.y);
    incrementCount(unit.CO, buildCoords);

    return null;
  }
  @Override
  public GameEventQueue receiveTurnInitEvent(Commander co, int turn)
  {
    resetCountFor(co);

    return null;
  }
}
