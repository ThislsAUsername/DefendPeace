package Terrain.Types;

import java.awt.Color;

public class Grass extends BaseTerrain
{
  private static Grass instance;

  protected Grass()
  {
    defLevel = 1;
    miniColor = new Color(166, 253, 77);
    baseIndex = getIndex();
  }
  
  public static BaseTerrain getInstance()
  {
    if (null == instance)
      instance = new Grass();
    return instance;
  }

}
