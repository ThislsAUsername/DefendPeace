package Engine;

import java.awt.Dimension;
import java.awt.Graphics;

public interface IView
{
  /**
   * Returns the size at which this view will render itself, in pixels.
   */
  public abstract Dimension getPreferredDimensions();

  /**
   * @return The width in pixels of the entire view area.
   */
  public abstract int getViewWidth();
  /**
   * @return The height in pixels of the entire view area.
   */
  public abstract int getViewHeight();

  /**
   * Renders the view using the specified graphics.
   */
  public void render(Graphics g);
}
