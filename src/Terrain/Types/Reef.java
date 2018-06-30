package Terrain.Types;

import java.awt.Color;

public class Reef extends TerrainType
{
  private static int flags = WATER | PROVIDES_COVER;
  private static int defense = 2;
  private static Color color = new Color(218, 152, 112);

  private static Reef instance;

  protected Reef()
  {
    super(flags, defense, color);
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Reef();
    return instance;
  }
}
