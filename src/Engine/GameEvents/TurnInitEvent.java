package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * Does nothing except signal the start of a Commander's turn.
 */
public class TurnInitEvent implements GameEvent
{
  Commander cmdr;
  int turn;

  public TurnInitEvent(Commander co, int turnNum)
  {
    cmdr = co;
    turn = turnNum;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTurnInitAnimation(cmdr, turn);
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveTurnInitEvent(cmdr, turn);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
  }

  @Override
  public XYCoord getStartPoint()
  {
    return null;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return null;
  }
}
