package Engine.GameEvents;

import java.util.ArrayDeque;
import java.util.function.Consumer;

import Terrain.MapMaster;

public class GameEventQueue extends ArrayDeque<GameEvent>
{
  private static final long serialVersionUID = 1L;

  public boolean add(Consumer<MapMaster> lambda)
  {
    return add(new LambdaEvent(lambda));
  }
}
