package Terrain.Types;

import java.awt.Color;

public class Airport extends Lucrative
{
  private static Airport instance;

  protected Airport()
  {
    defLevel = 3;
    // TODO: this is the Factory color
    miniColor = new Color(125, 125, 125);
    healsAir = true;
    // baseIndex is base class's
  }

  public static BaseTerrain getInstance()
  {
    if( null == instance )
      instance = new Airport();
    return instance;
  }
}
