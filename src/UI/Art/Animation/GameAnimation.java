package UI.Art.Animation;

import java.awt.Graphics;

import Engine.XYCoord;
import Units.Unit;

public interface GameAnimation
{
  /**
   * Draw the next frame of the animation. Return true if the animation is complete, else false.
   */
  public boolean animate(Graphics g);

  /**
   * Allows the caller to tell this animation to end early.
   */
  public void cancel();
}
