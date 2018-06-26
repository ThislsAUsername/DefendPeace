package Terrain.Types;

import java.awt.Color;

public class City extends Lucrative
{
  private static City instance;

  protected City()
  {
    defLevel = 2; // DoR cities are 2*
    miniColor = new Color(125, 125, 125);
    healsLand = true;
    // baseIndex is base class's
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new City();
    return instance;
  }
}
