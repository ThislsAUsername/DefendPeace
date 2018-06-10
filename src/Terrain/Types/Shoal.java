package Terrain.Types;

import java.awt.Color;

public class Shoal extends BaseTerrain
{

  private static Shoal instance;

  protected Shoal()
  {
    defLevel = 1;
    miniColor = new Color(253, 224, 93);
    isSea = true;
    isLand = true;
    baseIndex = index;
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Shoal();
    return instance;
  }

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return instance.index;
  }

}
