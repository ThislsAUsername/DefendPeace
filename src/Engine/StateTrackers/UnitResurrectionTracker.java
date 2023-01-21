package Engine.StateTrackers;

import java.util.HashMap;
import Engine.XYCoord;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.CreateUnitEvent.AnimationStyle;
import Engine.GameEvents.GameEventQueue;
import Units.Unit;
import Units.UnitModel;

/**
 * Resurrects certain unit types when they die, into a specified other type
 */
public class UnitResurrectionTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;
  private static final int FUDGE_RADIUS = 2;

  public HashMap<UnitModel, UnitModel> resurrectionTypeMap = new HashMap<>();

  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
  {
    if(!resurrectionTypeMap.containsKey(victim.model))
      return null;

    UnitModel resType = resurrectionTypeMap.get(victim.model);
    GameEventQueue events = new GameEventQueue();
    boolean unitIsReady = false;
    events.add(new CreateUnitEvent(victim.CO, resType, grave, AnimationStyle.DROP_IN, unitIsReady, FUDGE_RADIUS ));

    return events;
  }
}
