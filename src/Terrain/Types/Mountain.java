package Terrain.Types;

import java.awt.Color;

public class Mountain extends Grass
{
  private static Mountain instance;

  protected Mountain()
  {
    // TODO: Bonus to vision in fog?
    defLevel = 4;
    miniColor = new Color(153, 99, 67);
    baseIndex = Grass.getIndex();
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Mountain();
    return instance;
  }

  public static int getIndex()
  {
    if( null == instance )
      getInstance();
    return instance.index;
  }

}
