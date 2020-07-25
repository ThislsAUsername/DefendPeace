package Terrain;

import Engine.XYCoord;

public interface IEnvironsProvider
{
  /** Returns true if (x,y) lies within the GameMap, false else. */
  public abstract boolean isLocationValid(XYCoord coords);

  /** Returns true if (x,y) lies within the GameMap, false else. */
  public abstract boolean isLocationValid(int x, int y);

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  public Environment getEnvironment(XYCoord coord);

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  public Environment getEnvironment(int w, int h);
}
