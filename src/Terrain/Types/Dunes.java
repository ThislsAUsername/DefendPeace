package Terrain.Types;

import java.awt.Color;

public class Dunes extends TerrainType
{
  private static int flags = LAND;
  private static int defense = 1;
  private static Color color = new Color(240, 210, 120);

  private static Dunes instance;

  protected Dunes()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Dunes();
    return instance;
  }
}
