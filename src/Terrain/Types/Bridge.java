package Terrain.Types;

import java.awt.Color;

public class Bridge extends Shoal
{
  private static Bridge instance;

  protected Bridge()
  {
    defLevel = 0;
    miniColor = new Color(189, 189, 189);
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Bridge();
    return instance;
  }
}
