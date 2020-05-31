package UI.Art.Animation;

import java.awt.Graphics;

import Units.Unit;

public interface GameAnimation
{
  public static enum AnimState
  {
    IDLE
    {
      public String toString()
      {
        return ""; // To match the existing map image format
      }
    },
    TIRED, MOVENORTH, MOVEEAST, MOVESOUTH, MOVEWEST, DIE
  }

  /**
   * Draw the next frame of the animation. Return true if the animation is complete, else false.
   */
  public boolean animate(Graphics g);

  /**
   * Allows the caller to tell this animation to end early.
   */
  public void cancel();

  public default Unit getActor()
  {
    return null;
  }

  public default AnimState getAnimState()
  {
    return AnimState.IDLE;
  }
}
