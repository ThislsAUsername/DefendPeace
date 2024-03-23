package Engine.StateTrackers;

import java.util.ArrayList;
import java.util.HashMap;
import Engine.Army;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;

/**
 * Tracks where units started their last turn.
 */
public class UnitTurnPositionTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public HashMap<Unit, XYCoord> prevCoordMap = new HashMap<>();

  @Override
  public GameEventQueue receiveTurnInitEvent(MapMaster map, Army army, int turn)
  {
    ArrayList<Unit> units = army.getUnits();
    for( Unit u : units )
      prevCoordMap.put(u, new XYCoord(u));

    return null;
  }

  public boolean stoodStill(UnitContext u) {
    return stoodStill(u.unit, u.coord);
  }
  public boolean stoodStill(Unit u, XYCoord newCoord) {
    if(!prevCoordMap.containsKey(u))
      return false;
    final XYCoord oldCoord = prevCoordMap.get(u);
    return oldCoord.equals(newCoord);
  }
}
