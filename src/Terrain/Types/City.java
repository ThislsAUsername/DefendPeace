package Terrain.Types;

import java.awt.Color;

public class City extends TerrainType
{
  private static int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static int defense = 2;
  private static Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?

  private static City instance;

  protected City()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new City();
    return instance;
  }
}
