package Terrain.Types;

import java.awt.Color;

public class Headquarters extends City
{
  private static Headquarters instance;

  protected Headquarters()
  {
    defLevel = 4;
    miniColor = new Color(125, 125, 125);
    sustainsSide = true;
    // baseIndex is base class's
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Headquarters();
    return instance;
  }

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return instance.index;
  }

}
