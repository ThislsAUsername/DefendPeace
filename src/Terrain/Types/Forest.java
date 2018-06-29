package Terrain.Types;

import java.awt.Color;

public class Forest extends Grass
{
  private static Forest instance;

  protected Forest()
  {
    defLevel = 3;
    miniColor = new Color(46, 196, 24);
    isCover = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Forest();
    return instance;
  }
}
