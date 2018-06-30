package Terrain.Types;

import java.awt.Color;

public class Seaport extends TerrainType
{
  private static int flags = LAND | WATER | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_SEA;
  private static int defense = 3;
  private static Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?

  private static Seaport instance;

  protected Seaport()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Seaport();
    return instance;
  }
}
