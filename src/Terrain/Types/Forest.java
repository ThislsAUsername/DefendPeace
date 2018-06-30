package Terrain.Types;

import java.awt.Color;

public class Forest extends TerrainType
{
  private static int flags = LAND | PROVIDES_COVER;
  private static int defense = 3;
  private static Color color = new Color(46, 196, 24);

  private static Forest instance;

  protected Forest()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Forest();
    return instance;
  }
}
