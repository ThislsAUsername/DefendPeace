package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.MapMaster;
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
   * This function implements the visitor pattern interface to distribute events
   * to the correct receivers in GameEventListener subclasses.
   * @param listener The visitor who wishes to receive GameEvents.
   */
  public GameEventQueue sendToListener(GameEventListener listener);

  /**
   * Hook for subclasses to implement the specific effects of each action type.
   * @param map
   */
  public void performEvent( MapMaster gameMap );

  /**
   * Returns where the action begins for this event. This is primarily used by
   * the animator to decide whether to animate this event when fog of war is enabled.
   * Events with only one relevant location should return the same point from getEndPoint().
   * Events that are not location-bound (e.g. Commander abilities) should just return null.
   */
  public XYCoord getStartPoint();

  /**
   * Returns where the action ends for this event. This is primarily used by
   * the animator to decide whether to animate this event when fog of war is enabled.
   * Events with only one relevant location should return the same point from getStartPoint().
   * Events that are not location-bound (e.g. Commander abilities) should just return null.
   */
  public XYCoord getEndPoint();

  public default boolean shouldEndTurn() { return false; }
}
