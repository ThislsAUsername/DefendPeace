package Terrain.Types;

import java.awt.Color;

public class Airport extends TerrainType
{
  private static int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_AIR;
  private static int defense = 3;
  private static Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?

  private static Airport instance;

  protected Airport()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Airport();
    return instance;
  }
}
