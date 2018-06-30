package Terrain.Types;

import java.awt.Color;

public class Road extends TerrainType
{
  private static int flags = LAND;
  private static int defense = 0;
  private static Color color = new Color(189, 189, 189);

  private static Road instance;

  protected Road()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Road();
    return instance;
  }
}
