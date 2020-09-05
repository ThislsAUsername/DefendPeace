package UI;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import Engine.XYCoord;

/** Holds the data to define an overlay to display on the map */
public class GameOverlay implements Serializable
{
  private static final long serialVersionUID = 1L;

  /** Determines whether to draw in FoW; null for "always draw" */
  public XYCoord origin;
  public ArrayList<XYCoord> area = new ArrayList<XYCoord>();
  public Color fill = Color.white, edge = Color.black;

  public GameOverlay() {}

  public GameOverlay(XYCoord origin, Collection<XYCoord> area, Color fill, Color edge)
  {
    super();
    this.origin = origin;
    this.area.addAll(area);
    this.fill = fill;
    this.edge = edge;
  }
}
