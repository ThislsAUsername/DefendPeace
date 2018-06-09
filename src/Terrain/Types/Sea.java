package Terrain.Types;

import java.awt.Color;

public class Sea extends BaseTerrain
{
  private static Sea instance;

  protected Sea()
  {
    defLevel = 1;
    miniColor = new Color(94, 184, 236);
    baseIndex = getIndex();
  }
  
  public static BaseTerrain getInstance()
  {
    if (null == instance)
      instance = new Sea();
    return instance;
  }

}
