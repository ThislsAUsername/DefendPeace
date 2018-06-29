package Terrain.Types;

import java.awt.Color;

public abstract class TerrainType
{
  protected int defLevel = -1; // Level of protection provided by this terrain type. Typically 0-4.
  protected Color miniColor = new Color(0,0,0); // Predominant color of this terrain type. Here for convenience.
  protected Boolean isCapturable = false; // Whether a Commander can take ownership of this property.
  protected Boolean isProfitable = false; // Whether this terrain type grants income to its owner each turn.
  protected Boolean sustainsSide = false; // Whether it counts as an HQ
  protected Boolean isCover = false; // Whether it hides surface units in fog
  protected Boolean isLand = false;
  protected Boolean isSea = false;
  protected Boolean healsLand = false;
  protected Boolean healsSea = false;
  protected Boolean healsAir = false;

  // Need to be overloaded for all concrete subclasses, individually
  // Static methods can't be abstract
  public static TerrainType getInstance()
  {
    System.out.println("WARNING: Calling getInstance() on TerrainType; returning null.");
    return null;
  }

  // Beyond here is just boring getters.

  public int getDefLevel()
  {
    return defLevel;
  }

  public Color getMiniColor()
  {
    return miniColor;
  }

  public Boolean isCapturable()
  {
    return isCapturable;
  }

  public Boolean isProfitable()
  {
    return isProfitable;
  }
  
  public Boolean sustainsSide()
  {
    return sustainsSide;
  }
  
  public Boolean isCover()
  {
    return isCover;
  }
  
  public Boolean isLand()
  {
    return isLand;
  }
  
  public Boolean isSea()
  {
    return isSea;
  }
  
  public Boolean healsLand()
  {
    return healsLand;
  }
  
  public Boolean healsSea()
  {
    return healsSea;
  }
  
  public Boolean healsAir()
  {
    return healsAir;
  }
}
