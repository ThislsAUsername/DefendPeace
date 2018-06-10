package Terrain.Types;

import java.awt.Color;

public class Road extends Grass
{
  private static Road instance;

  protected Road()
  {
    defLevel = 0;
    miniColor = new Color(189, 189, 189);
    baseIndex = Grass.getIndex();
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Road();
    return instance;
  }

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return instance.index;
  }

}
