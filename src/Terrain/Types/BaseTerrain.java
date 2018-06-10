package Terrain.Types;

import java.awt.Color;

import Terrain.Environment;

public abstract class BaseTerrain
{
  
  public final int index; // Position in the terrainType array
  protected int baseIndex; // Position of the base display type
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
  protected BaseTerrain()
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
    index = registerType();
  }
  
  /**
   * Adds the new instance to the terrain type array
   * @return the index at which it was inserted
   */
  protected int registerType()
  {
    Environment.getTerrainTypes().add(this);
    return Environment.getTerrainTypes().size() - 1;
  }

  // Need to be overloaded for all concrete subclasses, individually
  // Static methods can't be abstract
  public static BaseTerrain getInstance()
  {
    return null;
  }

  // Need to be overloaded for all concrete subclasses, individually
  // Static methods can't be abstract
  public static int getIndex()
  {
    return -1;
  }
  
  // Beyond here is just boring getters.
  
  public int getBaseIndex()
  {
    return baseIndex;
  }
  
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
  
  public Boolean isObject()
  {
    return isTerrainObject;
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
