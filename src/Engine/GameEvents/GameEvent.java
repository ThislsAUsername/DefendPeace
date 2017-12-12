package Engine.GameEvents;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public interface GameEvent
{
  /**
   * This function implements the visitor pattern interface for this visited object.
   * In a typical visitor pattern implementation, this function would be called
   * accept(), but getEventAnimation() is more descriptive.
   * @param view The visitor, who also knows how to build each kind of animation.
   * @return the animation built by the visitor, or null if the event type is not valid.
   */
  public GameAnimation getEventAnimation( MapView mapView );

  /**
   * Hook for subclasses to implement the specific effects of each action type.
   * @param map
   */
  public void performEvent( GameMap gameMap );
}
