package Terrain.Types;

import java.awt.Color;

public class Grass extends BaseTerrain
{
  private static Grass instance;

  protected Grass()
  {
    defLevel = 1;
    miniColor = new Color(166, 253, 77);
    isLand = true;
    baseIndex = index;
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Grass();
    return instance;
  }

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return instance.index;
  }

}
