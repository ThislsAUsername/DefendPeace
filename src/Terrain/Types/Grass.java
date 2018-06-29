package Terrain.Types;

import java.awt.Color;

public class Grass extends TerrainType
{
  private static Grass instance;

  protected Grass()
  {
    defLevel = 1;
    miniColor = new Color(166, 253, 77);
    isLand = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Grass();
    return instance;
  }
}
