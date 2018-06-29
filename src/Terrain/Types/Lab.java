package Terrain.Types;

import java.awt.Color;

/**
 *  Labs don't give income or heal anything.
 *  They're arguably the most useless building you can capture.
 */
public class Lab extends Ownable
{
  private static Lab instance;

  protected Lab()
  {
    defLevel = 3;
    // TODO: is HQ's color
    miniColor = new Color(125, 125, 125);
    sustainsSide = true;
    // baseIndex is base class's
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Lab();
    return instance;
  }

  private static int index = -1;

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return index;
  }

}
