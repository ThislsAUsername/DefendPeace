package Terrain.Types;

import java.awt.Color;

public class Sea extends TerrainType
{
  private static Sea instance;

  protected Sea()
  {
    defLevel = 1;
    miniColor = new Color(94, 184, 236);
    isSea = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Sea();
    return instance;
  }
}
