package Engine.UnitMods;

import java.util.HashMap;
import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.GameEvents.GameEventQueue;
import Units.Unit;
import Units.UnitModel;

/**
 * Tracks what type units used to be until their next turn.
 */
public class TransformationTracker extends StateTracker<TransformationTracker>
{
  private static final long serialVersionUID = 1L;

  protected TransformationTracker(Class<TransformationTracker> key, GameInstance gi)
  {
    super(key, gi);
  }
  @Override
  protected TransformationTracker item()
  {
    return this;
  }

  public HashMap<Unit, UnitModel> prevTypeMap = new HashMap<>();

  @Override
  public GameEventQueue receiveUnitTransformEvent(Unit unit, UnitModel oldType)
  {
    prevTypeMap.put(unit, oldType);

    return null;
  }
  @Override
  public GameEventQueue receiveTurnInitEvent(Commander co, int turn)
  {
    for( Unit u : prevTypeMap.keySet().toArray(new Unit[0]) )
      if( co == u.CO )
        prevTypeMap.remove(u);

    return null;
  }
}
