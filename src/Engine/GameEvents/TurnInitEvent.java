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
  boolean opaque;

  public TurnInitEvent(Commander co, int turnNum, boolean hideMap)
  {
    cmdr = co;
    turn = turnNum;
    opaque = hideMap;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTurnInitAnimation(cmdr, turn, opaque);
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
