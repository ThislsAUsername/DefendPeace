package Engine.GameEvents;

import java.util.ArrayList;
import java.util.Collection;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * Sets up the input army's units for the next turn.
 */
public class TurnInitEvent implements GameEvent
{
  final MapMaster map;
  Army army;
  int turn;
  boolean opaque;
  Collection<String> msg;

  public TurnInitEvent(MapMaster map, Army army, int turnNum, boolean hideMap)
  {
    this(map, army, turnNum, hideMap, "Turn "+turnNum);
  }

  public TurnInitEvent(MapMaster map, Army army, int turnNum, boolean hideMap, String message)
  {
    this(map, army, turnNum, hideMap, new ArrayList<String>());
    msg.add(message);
  }

  public TurnInitEvent(MapMaster map, Army army, int turnNum, boolean hideMap, Collection<String> message)
  {
    this.map = map;
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
    return listener.receiveTurnInitEvent(map, army, turn);
  }

  @Override
  public void performEvent(MapMaster map)
  {
    for( Unit u : army.getUnits() )
      u.initTurn(map);
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
