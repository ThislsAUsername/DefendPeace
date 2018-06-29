package Terrain.Types;

import java.awt.Color;

public abstract class TerrainType
{
  protected int defLevel; // 1% damage reduction per HP, per unit of defense
  protected float incomeMultiplier; // 1.0 for standard income
  protected Color miniColor; // Minimap and FillRect color
  protected Boolean isTerrainObject; // Whether this terrain type can meaningfully change state
  protected Boolean capturable; // Whether someone can own and derive benefit from this property
  protected Boolean sustainsSide; // Whether it counts as an HQ
  protected Boolean isCover; // Whether it hides surface units in fog
  protected Boolean isLand;
  protected Boolean isSea;
  protected Boolean healsLand;
  protected Boolean healsSea;
  protected Boolean healsAir;

  /** Sets defaults, and adds the new terrain type to the array. */
  protected TerrainType()
  {
    defLevel = -1;
    incomeMultiplier = 0;
    miniColor = new Color(0,0,0);
    isTerrainObject = false;
    capturable = false;
    sustainsSide = false;
    isCover = false;
    isLand = false;
    isSea = false;
    healsLand = false;
    healsSea = false;
    healsAir = false;
  }

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
  
  public float getincomeMultiplier()
  {
    return incomeMultiplier;
  }
  
  public Color getMiniColor()
  {
    return miniColor;
  }

  public Boolean isCapturable()
  {
    return capturable;
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
