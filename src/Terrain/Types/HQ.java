package Terrain.Types;

import java.awt.Color;

public class HQ extends City
{
  private static HQ instance;

  protected HQ()
  {
    defLevel = 4;
    miniColor = new Color(125, 125, 125);
    sustainsSide = true;
    // baseIndex is base class's
  }
  
  public static BaseTerrain getInstance()
  {
    if (null == instance)
      instance = new HQ();
    return instance;
  }

}
