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
   * Renders the view using the specified graphics.
   */
  public void render(Graphics g);
}
