package Engine.GameEvents;

import java.util.ArrayList;
import java.util.Collection;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * Does nothing except signal the start of a Commander's turn.
 */
public class TurnInitEvent implements GameEvent
{
  Army army;
  int turn;
  boolean opaque;
  Collection<String> msg;

  public TurnInitEvent(Army army, int turnNum, boolean hideMap)
  {
    this(army, turnNum, hideMap, "Turn "+turnNum);
  }

  public TurnInitEvent(Army army, int turnNum, boolean hideMap, String message)
  {
    this(army, turnNum, hideMap, new ArrayList<String>());
    msg.add(message);
  }

  public TurnInitEvent(Army army, int turnNum, boolean hideMap, Collection<String> message)
  {
    this.army = army;
    turn = turnNum;
    opaque = hideMap;
    msg = message;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildTurnInitAnimation(army, turn, opaque, msg);
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveTurnInitEvent(army, turn);
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
