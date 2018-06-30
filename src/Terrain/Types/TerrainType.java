package Terrain.Types;

import java.awt.Color;

public abstract class TerrainType
{
  // Protected constants to initialize/parse the attribute flags.
  protected static final int LAND = 1;
  protected static final int WATER = LAND << 1;
  protected static final int CAPTURABLE = WATER << 1; // Whether a Commander can take ownership of this property.
  protected static final int PROFITABLE = CAPTURABLE << 1; // Whether this terrain type grants income to its owner each turn.
  protected static final int PROVIDES_COVER = PROFITABLE << 1; // Whether it hides surface units in fog
  protected static final int HEALS_LAND = PROVIDES_COVER << 1;
  protected static final int HEALS_SEA = HEALS_LAND << 1;
  protected static final int HEALS_AIR = HEALS_SEA << 1;

  private int mDefenseLevel = -1; // Level of protection provided by this terrain type. Typically 0-4.
  private  Color mMainColor = null; // Predominant color of this terrain type. Here for convenience.
  private int mAttributes = 0; // bitmask of binary tile characteristics.

  protected TerrainType(int attributeFlags, int defense, Color mainColor)
  {
    mAttributes = attributeFlags;
    mDefenseLevel = defense;
    mMainColor = mainColor;
  }

  // Need to be overloaded for all concrete subclasses, individually
  // Static methods can't be abstract
  public static TerrainType getInstance()
  {
    System.out.println("WARNING: Calling getInstance() on TerrainType; returning null.");
    return null;
  }

  public int getDefLevel()
  {
    return mDefenseLevel;
  }

  public Color getMainColor()
  {
    return mMainColor;
  }

  public Boolean isCapturable()
  {
    return 0 != (mAttributes & CAPTURABLE);
  }

  public Boolean isProfitable()
  {
    return 0 != (mAttributes & PROFITABLE);
  }

  public Boolean isCover()
  {
    return 0 != (mAttributes & PROVIDES_COVER);
  }
  
  public Boolean isLand()
  {
    return 0 != (mAttributes & LAND);
  }
  
  public Boolean isWater()
  {
    return 0 != (mAttributes & WATER);
  }
  
  public Boolean healsLand()
  {
    return 0 != (mAttributes & HEALS_LAND);
  }
  
  public Boolean healsSea()
  {
    return 0 != (mAttributes & HEALS_SEA);
  }
  
  public Boolean healsAir()
  {
    return 0 != (mAttributes & HEALS_AIR);
  }
}
