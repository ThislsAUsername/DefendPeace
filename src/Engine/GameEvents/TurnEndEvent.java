package Engine.GameEvents;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * Does nothing except signal the end of an Army's turn.
 */
public class TurnEndEvent implements GameEvent
{
  final MapMaster map;
  Army army;
  int turn;
  public TurnEndEvent(MapMaster map, Army army, int turnNum)
  {
    this.map = map;
    this.army = army;
    turn = turnNum;
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
  public void performEvent(MapMaster map)
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

  @Override
  public boolean shouldEndTurn()
  {
    return true;
  }
}
