package UI.Art.Animation;

import java.awt.Graphics;
import java.util.ArrayList;
import Units.Unit;

public abstract class GameAnimation
{
  boolean isMapAnimation;
  boolean isMapVisible;

  public GameAnimation(boolean animateOnMap)
  {
    this(animateOnMap, true);
  }

  public GameAnimation(boolean animateOnMap, boolean showMap)
  {
    isMapAnimation = animateOnMap;
    isMapVisible = showMap;
  }

  /**
   * Declares where the animation should be drawn; on the map (it has a location) or over the whole screen.
   */
  public boolean isMapAnimation()
  {
    return isMapAnimation;
  }

  /**
   * Returns true if this animation covers the whole map, or if that should still be drawn underneath.
   */
  public boolean isMapVisible()
  {
    return isMapVisible;
  }

  /**
   * Draw the next frame of the animation. Return true if the animation is complete, else false.
   */
  public abstract boolean animate(Graphics g);

  /**
   * Allows the caller to tell this animation to end early.
   */
  public abstract void cancel();

  /**
   * The set of units that shouldn't be otherwise drawn while the GameAnimation is in progress
   */
  public ArrayList<Unit> getActors()
  {
    return new ArrayList<Unit>();
  }
}
