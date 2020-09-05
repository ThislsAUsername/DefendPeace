package Engine.GameEvents;

import java.util.ArrayList;
import java.util.Collection;

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
  Collection<String> msg;

  public TurnInitEvent(Commander co, int turnNum, boolean hideMap)
  {
    this(co, turnNum, hideMap, new ArrayList<String>());
    msg.add("Turn "+turnNum);
  }

  public TurnInitEvent(Commander co, int turnNum, boolean hideMap, String message)
  {
    this(co, turnNum, hideMap, new ArrayList<String>());
    msg.add(message);
  }

  public TurnInitEvent(Commander co, int turnNum, boolean hideMap, Collection<String> message)
  {
    cmdr = co;
    turn = turnNum;
    opaque = hideMap;
    msg = message;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTurnInitAnimation(cmdr, turn, opaque, msg);
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
