package Terrain.Types;

import java.awt.Color;

public class Headquarters extends TerrainType
{
  private static Headquarters instance;

  protected Headquarters()
  {
    defLevel = 4;
    miniColor = new Color(125, 125, 125);
    sustainsSide = true;
    isCapturable = true;
    isProfitable = true;
    isLand = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Headquarters();
    return instance;
  }
}
