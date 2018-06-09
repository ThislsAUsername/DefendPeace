package Terrain.Types;

import java.awt.Color;

public class Dunes extends Grass
{
  private static Dunes instance;

  protected Dunes()
  {
    defLevel = 1;
    miniColor = new Color(240, 210, 120);
    // baseIndex is base class's
  }
  
  public static BaseTerrain getInstance()
  {
    if (null == instance)
      instance = new Dunes();
    return instance;
  }

}
