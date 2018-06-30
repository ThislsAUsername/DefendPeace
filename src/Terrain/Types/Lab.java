package Terrain.Types;

import java.awt.Color;

public class Lab extends TerrainType
{
  private static int flags = LAND | CAPTURABLE | PROVIDES_COVER;
  private static int defense = 3;
  private static Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?

  private static Lab instance;

  protected Lab()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Lab();
    return instance;
  }
}
