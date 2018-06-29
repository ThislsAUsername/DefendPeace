package Terrain.Types;

import java.awt.Color;

public class Seaport extends Lucrative
{
  private static Seaport instance;

  protected Seaport()
  {
    defLevel = 3;
    // TODO: this is the Factory color
    miniColor = new Color(125, 125, 125);
    isSea = true;
    isLand = true;
    healsSea = true;
  }

  public static TerrainType getInstance()
  {
    if( null == instance )
      instance = new Seaport();
    return instance;
  }
}
