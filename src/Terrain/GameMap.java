package Terrain;

import java.io.Serializable;

import Engine.Path;
import Engine.XYCoord;
import Units.Unit;

public abstract class GameMap implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final int mapWidth;
  public final int mapHeight;
  public CommandingOfficers.Commander[] commanders;

  public GameMap(int w, int h)
  {
    mapWidth = w;
    mapHeight = h;
  }
  
  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  public abstract boolean isLocationValid(XYCoord coords);

  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  public abstract boolean isLocationValid(int x, int y);

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  public abstract Environment getEnvironment(XYCoord coord);

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  public abstract Environment getEnvironment(int w, int h);

  /** Returns the Location at the specified location, or null if that Location does not exist. */
  public abstract Location getLocation(XYCoord location);

  /** Returns the Location at the specified location, or null if that Location does not exist. */
  public abstract Location getLocation(int w, int h);

  public abstract void clearAllHighlights();

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  public abstract boolean isLocationEmpty(XYCoord coords);

  /** Returns true if no unit is at the specified x and y coordinate, false else */
  public abstract boolean isLocationEmpty(int x, int y);

  /** Returns true if no unit (excluding 'unit') is in the specified Location. */
  public abstract boolean isLocationEmpty(Unit unit, XYCoord coords);

  /** Returns true if no unit (excluding 'unit') is in the specified Location. */
  public abstract boolean isLocationEmpty(Unit unit, int x, int y);

  /**
   * Returns true if the location lies outside the GameMap.
   */
  public abstract boolean isLocationFogged(XYCoord coord);
  public abstract boolean isLocationFogged(int x, int y);

  /**
   * Resets fog, if applicable
   */
  public void resetFog()
  {}

  /**
   * Reveals fog around the given unit, if applicable
   */
  public void revealFog(Unit scout)
  {}

  /**
   * Reveals fog along the movement path, if applicable
   */
  public void revealFog(Unit scout, Path movepath)
  {}
}