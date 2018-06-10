package Terrain.Types;

import java.awt.Color;

public class Reef extends Sea
{
  private static Reef instance;

  protected Reef()
  {
    defLevel = 2;
    miniColor = new Color(218, 152, 112);
    isCover = true;
    baseIndex = Sea.getIndex();
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Reef();
    return instance;
  }

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return instance.index;
  }

}
