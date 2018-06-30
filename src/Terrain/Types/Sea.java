package Terrain.Types;

import java.awt.Color;

public class Sea extends TerrainType
{
  private static int flags = WATER;
  private static int defense = 1;
  private static Color color = new Color(94, 184, 236);

  private static Sea instance;

  protected Sea()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Sea();
    return instance;
  }
}
