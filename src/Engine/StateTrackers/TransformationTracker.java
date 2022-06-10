package Engine.StateTrackers;

import java.util.HashMap;
import Engine.Army;
import Engine.GameEvents.GameEventQueue;
import Units.Unit;
import Units.UnitModel;

/**
 * Tracks what type units used to be until their next turn.
 */
public class TransformationTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public HashMap<Unit, UnitModel> prevTypeMap = new HashMap<>();

  @Override
  public GameEventQueue receiveUnitTransformEvent(Unit unit, UnitModel oldType)
  {
    prevTypeMap.put(unit, oldType);

    return null;
  }
  @Override
  public GameEventQueue receiveTurnEndEvent(Army army, int turn)
  {
    for( Unit u : prevTypeMap.keySet().toArray(new Unit[0]) )
      if( army == u.CO.army )
        prevTypeMap.remove(u);

    return null;
  }
}
