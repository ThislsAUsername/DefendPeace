package Terrain.Types;

import java.awt.Color;

public class Grass extends TerrainType
{
  private static int flags = LAND;
  private static int defense = 1;
  private static Color color = new Color(166, 253, 77);

  private static Grass instance;

  protected Grass()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Grass();
    return instance;
  }
}
