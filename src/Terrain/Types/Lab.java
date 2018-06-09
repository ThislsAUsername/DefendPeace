package Terrain.Types;

import java.awt.Color;

public class Lab extends Ownable
{
  private static Lab instance;

  protected Lab()
  {
    defLevel = 3;
    // TODO: is HQ's color
    miniColor = new Color(125, 125, 125);
    sustainsSide = true;
    // baseIndex is base class's
  }
  
  public static BaseTerrain getInstance()
  {
    if (null == instance)
      instance = new Lab();
    return instance;
  }

}
