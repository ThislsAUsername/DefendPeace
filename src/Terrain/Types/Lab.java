package Terrain.Types;

import java.awt.Color;

public class Lab extends TerrainType
{
  private static Lab instance;

  protected Lab()
  {
    defLevel = 3;
    // TODO: is HQ's color
    miniColor = new Color(125, 125, 125);
    sustainsSide = true;
    isCapturable = true;
    isLand = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Lab();
    return instance;
  }
}
