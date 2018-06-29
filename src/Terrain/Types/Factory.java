package Terrain.Types;

import java.awt.Color;

public class Factory extends TerrainType
{
  private static Factory instance;

  protected Factory()
  {
    defLevel = 3;
    miniColor = new Color(125, 125, 125);
    isCapturable = true;
    isProfitable = true;
    isLand = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Factory();
    return instance;
  }
}
