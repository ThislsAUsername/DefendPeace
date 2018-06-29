package Terrain.Types;

import java.awt.Color;

public class Airport extends TerrainType
{
  private static Airport instance;

  protected Airport()
  {
    defLevel = 3;
    // TODO: this is the Factory color
    miniColor = new Color(125, 125, 125);
    healsAir = true;
    isCapturable = true;
    isProfitable = true;
    isLand = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Airport();
    return instance;
  }
}
