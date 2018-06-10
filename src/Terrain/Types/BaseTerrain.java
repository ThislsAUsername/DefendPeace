package Terrain.Types;

import java.awt.Color;

import Terrain.Environment;

public abstract class BaseTerrain
{
  
  public final int index;
  protected int baseIndex;
  protected int defLevel;
  protected float incomeMultiplier;
  protected Color miniColor;
  protected Boolean isTerrainObject;
  protected Boolean capturable;
  protected Boolean sustainsSide;
  protected Boolean isCover;
  protected Boolean isLand;
  protected Boolean isSea;
  protected Boolean healsLand;
  protected Boolean healsSea;
  protected Boolean healsAir;
  
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
  
  protected int registerType()
  {
    Environment.getTerrainTypes().add(this);
    return Environment.getTerrainTypes().size() - 1;
  }
  
  public static BaseTerrain getInstance()
  {
    return null;
  }
  
  public static int getIndex()
  {
    return -1;
  }
  
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
