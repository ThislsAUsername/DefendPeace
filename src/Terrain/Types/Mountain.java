package Terrain.Types;

import java.awt.Color;

public class Mountain extends TerrainType
{
  private static int flags = LAND;
  private static int defense = 4;
  private static Color color = new Color(153, 99, 67);

  private static Mountain instance;

  protected Mountain()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Mountain();
    return instance;
  }
}
