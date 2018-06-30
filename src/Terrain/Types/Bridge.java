package Terrain.Types;

import java.awt.Color;

public class Bridge extends TerrainType
{
  private static int flags = LAND | WATER;
  private static int defense = 0;
  private static Color color = new Color(189, 189, 189);

  private static Bridge instance;

  protected Bridge()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Bridge();
    return instance;
  }
}
