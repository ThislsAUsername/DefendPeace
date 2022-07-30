package Engine.GameEvents;

import Engine.Army;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * Does nothing except signal the end of an Army's turn.
 */
public class TurnEndEvent extends TurnInitEvent
{
  public TurnEndEvent(Army army, int turnNum)
  {
    super(army, turnNum, false, "Turn "+turnNum);
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveTurnEndEvent(army, turn);
  }

  @Override
  public boolean shouldEndTurn()
  {
    return true;
  }
}
