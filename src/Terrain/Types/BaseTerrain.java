package Terrain.Types;

import java.awt.Color;

import Terrain.Environment;

public class BaseTerrain
{
  private static int index = -1;
  private static BaseTerrain instance;
  
  protected int baseIndex;
  protected int defLevel;
  protected float incomeMultiplier;
  protected Color miniColor;
  protected Boolean isTerrainObject;
  protected Boolean capturable;
  protected Boolean sustainsSide;
  protected Boolean isCover;
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
    healsLand = false;
    healsSea = false;
    healsAir = false;
    registerType(this);
    baseIndex = index;
  }
  
  protected void registerType(BaseTerrain entity)
  {
    Environment.types.add(entity);
    index = Environment.types.size() - 1;
  }
  
  public static BaseTerrain getInstance()
  {
    if (null == instance)
      instance = new BaseTerrain();
    return instance;
  }
  
  public static int getIndex()
  {
    if (null == instance)
      getInstance();
    return index;
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
