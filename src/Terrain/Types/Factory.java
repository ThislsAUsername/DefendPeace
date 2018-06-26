package Terrain.Types;

import java.awt.Color;

public class Factory extends City
{
  private static Factory instance;

  protected Factory()
  {
    defLevel = 3;
    miniColor = new Color(125, 125, 125);
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Factory();
    return instance;
  }
}
