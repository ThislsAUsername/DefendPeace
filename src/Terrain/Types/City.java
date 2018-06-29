package Terrain.Types;

import java.awt.Color;

public class City extends TerrainType
{
  private static City instance;

  protected City()
  {
    defLevel = 2;
    miniColor = new Color(125, 125, 125);
    healsLand = true;
    isCapturable = true;
    isProfitable = true;
    isLand = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new City();
    return instance;
  }
}
