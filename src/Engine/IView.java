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
   * Allows a user to request that the mapView adjust its size.
   * The IView may or may not accept this request.
   */
  public abstract void setPreferredDimensions(int width, int height);

  /**
   * Renders the view using the specified graphics.
   */
  public void render(Graphics g);

  /** Do any post-action cleanup, free resources, etc. */
  public abstract void cleanup();
}
