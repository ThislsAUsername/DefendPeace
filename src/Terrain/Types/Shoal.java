package Terrain.Types;

import java.awt.Color;

public class Shoal extends TerrainType
{
  private static int flags = LAND | WATER | PROVIDES_COVER;
  private static int defense = 1;
  private static Color color = new Color(253, 224, 93);

  private static Shoal instance;

  protected Shoal()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Shoal();
    return instance;
  }
}
