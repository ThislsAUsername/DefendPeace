package Terrain;

import java.awt.Color;

public class TerrainType
{
  // Local class data
  private int mDefenseLevel = -1; // Level of protection provided by this terrain type. Typically 0-4.
  private  Color mMainColor = null; // Predominant color of this terrain type. Here for convenience.
  private int mAttributes = 0; // bitmask of binary tile characteristics.

  // Generic constructor.
  private TerrainType(int attributeFlags, int defense, Color mainColor)
  {
    mAttributes = attributeFlags;
    mDefenseLevel = defense;
    mMainColor = mainColor;
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  //// Protected constants to initialize/parse the attribute flags, and the methods to query them.
  private static final int LAND = 1;
  private static final int WATER = LAND << 1;
  private static final int CAPTURABLE = WATER << 1; // Whether a Commander can take ownership of this property.
  private static final int PROFITABLE = CAPTURABLE << 1; // Whether this terrain type grants income to its owner each turn.
  private static final int PROVIDES_COVER = PROFITABLE << 1; // Whether it hides surface units in fog
  private static final int HEALS_LAND = PROVIDES_COVER << 1;
  private static final int HEALS_SEA = HEALS_LAND << 1;
  private static final int HEALS_AIR = HEALS_SEA << 1;

  public int getDefLevel() { return mDefenseLevel; }
  public Color getMainColor() { return mMainColor; }
  public Boolean isCapturable() { return 0 != (mAttributes & CAPTURABLE); }
  public Boolean isProfitable() { return 0 != (mAttributes & PROFITABLE); }
  public Boolean isCover() { return 0 != (mAttributes & PROVIDES_COVER); }
  public Boolean isLand() { return 0 != (mAttributes & LAND); }
  public Boolean isWater() { return 0 != (mAttributes & WATER); }
  public Boolean healsLand() { return 0 != (mAttributes & HEALS_LAND); }
  public Boolean healsSea() { return 0 != (mAttributes & HEALS_SEA); }
  public Boolean healsAir() { return 0 != (mAttributes & HEALS_AIR); }
  
  ////////////////////////////////////////////////////////////////////////////////////////////
  //// Publicly-accessible terrain Singleton instances, and the functions to build them.
  public static final TerrainType AIRPORT = getAirport();
  public static final TerrainType BRIDGE = getBridge();
  public static final TerrainType CITY = getCity();
  public static final TerrainType DUNES = getDunes();
  public static final TerrainType FACTORY = getFactory();
  public static final TerrainType FOREST = getForest();
  public static final TerrainType GRASS = getGrass();
  public static final TerrainType HEADQUARTERS = getHeadquarters();
  public static final TerrainType LAB = getLab();
  public static final TerrainType MOUNTAIN = getMountain();
  public static final TerrainType REEF = getReef();
  public static final TerrainType ROAD = getRoad();
  public static final TerrainType SEA = getSea();
  public static final TerrainType SEAPORT = getSeaport();
  public static final TerrainType SHOAL = getShoal();

  // List of all terrain types.
  public static final TerrainType[] TerrainTypeList = {
    AIRPORT, BRIDGE, CITY, DUNES, FACTORY, FOREST, GRASS, HEADQUARTERS, LAB, MOUNTAIN, REEF, ROAD, SEA, SEAPORT, SHOAL
  };

  private static TerrainType getAirport()
  {
    int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_AIR;
    int defense = 3;
    Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getBridge()
  {
    int flags = LAND | WATER;
    int defense = 0;
    Color color = new Color(189, 189, 189);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getCity()
  {
    int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
    int defense = 2;
    Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getDunes()
  {
    int flags = LAND;
    int defense = 1;
    Color color = new Color(240, 210, 120);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getFactory()
  {
    int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
    int defense = 3;
    Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getForest()
  {
    int flags = LAND | PROVIDES_COVER;
    int defense = 3;
    Color color = new Color(46, 196, 24);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getGrass()
  {
    int flags = LAND;
    int defense = 1;
    Color color = new Color(166, 253, 77);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getHeadquarters()
  {
    int flags = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
    int defense = 4;
    Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getLab()
  {
    int flags = LAND | CAPTURABLE | PROVIDES_COVER;
    int defense = 3;
    Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getMountain()
  {
    int flags = LAND;
    int defense = 4;
    Color color = new Color(153, 99, 67);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getReef()
  {
    int flags = WATER | PROVIDES_COVER;
    int defense = 2;
    Color color = new Color(218, 152, 112);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getRoad()
  {
    int flags = LAND;
    int defense = 0;
    Color color = new Color(189, 189, 189);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getSea()
  {
    int flags = WATER;
    int defense = 1;
    Color color = new Color(94, 184, 236);
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getSeaport()
  {
    int flags = LAND | WATER | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_SEA;
    int defense = 3;
    Color color = new Color(125, 125, 125); // TODO: define unique color for each building type?
    return new TerrainType( flags, defense, color );
  }

  private static TerrainType getShoal()
  {
    int flags = LAND | WATER | PROVIDES_COVER;
    int defense = 1;
    Color color = new Color(253, 224, 93);
    return new TerrainType( flags, defense, color );
  }
}
