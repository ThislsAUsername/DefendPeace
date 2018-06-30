package Terrain.Types;

import java.awt.Color;

public class Headquarters extends TerrainType
{
  private static int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static int defense = 4;
  private static Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?

  private static Headquarters instance;

  protected Headquarters()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Headquarters();
    return instance;
  }
}
