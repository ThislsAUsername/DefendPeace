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
  //// Publicly-accessible terrain Flyweight instances.
  private static final int AIRPORT_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_AIR;
  private static final int AIRPORT_DEFENSE = 3;
  private static final Color AIRPORT_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  public static final TerrainType AIRPORT = new TerrainType( AIRPORT_FLAGS, AIRPORT_DEFENSE, AIRPORT_COLOR );

  private static final int BRIDGE_FLAGS = LAND | WATER;
  private static final int BRIDGE_DEFENSE = 0;
  private static final Color BRIDGE_COLOR = new Color(189, 189, 189);
  public static final TerrainType BRIDGE = new TerrainType( BRIDGE_FLAGS, BRIDGE_DEFENSE, BRIDGE_COLOR);

  private static final int CITY_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static final int CITY_DEFENSE = 2;
  private static final Color CITY_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  public static final TerrainType CITY = new TerrainType( CITY_FLAGS, CITY_DEFENSE, CITY_COLOR );

  private static final int DUNES_FLAGS = LAND;
  private static final int DUNES_DEFENSE = 1;
  private static final Color DUNES_COLOR = new Color(240, 210, 120);
  public static final TerrainType DUNES = new TerrainType( DUNES_FLAGS, DUNES_DEFENSE, DUNES_COLOR );

  private static final int FACTORY_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static final int FACTORY_DEFENSE = 3;
  private static final Color FACTORY_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  public static final TerrainType FACTORY = new TerrainType( FACTORY_FLAGS, FACTORY_DEFENSE, FACTORY_COLOR );

  private static final int FOREST_FLAGS = LAND | PROVIDES_COVER;
  private static final int FOREST_DEFENSE = 3;
  private static final Color FOREST_COLOR = new Color(46, 196, 24);
  public static final TerrainType FOREST = new TerrainType( FOREST_FLAGS, FOREST_DEFENSE, FOREST_COLOR );

  private static final int GRASS_FLAGS = LAND;
  private static final int GRASS_DEFENSE = 1;
  private static final Color GRASS_COLOR = new Color(166, 253, 77);
  public static final TerrainType GRASS = new TerrainType( GRASS_FLAGS, GRASS_DEFENSE, GRASS_COLOR );

  private static final int HEADQUARTERS_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static final int HEADQUARTERS_DEFENSE = 4;
  private static final Color HEADQUARTERS_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  public static final TerrainType HEADQUARTERS = new TerrainType( HEADQUARTERS_FLAGS, HEADQUARTERS_DEFENSE, HEADQUARTERS_COLOR );

  private static final int LAB_FLAGS = LAND | CAPTURABLE | PROVIDES_COVER;
  private static final int LAB_DEFENSE = 3;
  private static final Color LAB_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  public static final TerrainType LAB = new TerrainType( LAB_FLAGS, LAB_DEFENSE, LAB_COLOR );

  private static final int MOUNTAIN_FLAGS = LAND;
  private static final int MOUNTAIN_DEFENSE = 4;
  private static final Color MOUNTAIN_COLOR = new Color(153, 99, 67);
  public static final TerrainType MOUNTAIN = new TerrainType( MOUNTAIN_FLAGS, MOUNTAIN_DEFENSE, MOUNTAIN_COLOR );

  private static final int REEF_FLAGS = WATER | PROVIDES_COVER;
  private static final int REEF_DEFENSE = 2;
  private static final Color REEF_COLOR = new Color(218, 152, 112);
  public static final TerrainType REEF = new TerrainType( REEF_FLAGS, REEF_DEFENSE, REEF_COLOR );

  private static final int RIVER_FLAGS = LAND; // It's not really a WATER type, since boats can't go here.
  private static final int RIVER_DEFENSE = 0;
  private static final Color RIVER_COLOR = new Color(148, 219, 255);
  public static final TerrainType RIVER = new TerrainType( RIVER_FLAGS, RIVER_DEFENSE, RIVER_COLOR );

  private static final int ROAD_FLAGS = LAND;
  private static final int ROAD_DEFENSE = 0;
  private static final Color ROAD_COLOR = new Color(189, 189, 189);
  public static final TerrainType ROAD = new TerrainType( ROAD_FLAGS, ROAD_DEFENSE, ROAD_COLOR );

  private static final int WATER_FLAGS = WATER;
  private static final int WATER_DEFENSE = 1;
  private static final Color WATER_COLOR = new Color(94, 184, 236);
  public static final TerrainType SEA = new TerrainType( WATER_FLAGS, WATER_DEFENSE, WATER_COLOR );

  private static final int SEAPORT_FLAGS = LAND | WATER | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_SEA;
  private static final int SEAPORT_DEFENSE = 3;
  private static final Color SEAPORT_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  public static final TerrainType SEAPORT = new TerrainType( SEAPORT_FLAGS, SEAPORT_DEFENSE, SEAPORT_COLOR );

  private static final int SHOAL_FLAGS = LAND | WATER | PROVIDES_COVER;
  private static final int SHOAL_DEFENSE = 1;
  private static final Color SHOAL_COLOR = new Color(253, 224, 93);
  public static final TerrainType SHOAL = new TerrainType( SHOAL_FLAGS, SHOAL_DEFENSE, SHOAL_COLOR );

  // List of all terrain types.
  public static final TerrainType[] TerrainTypeList = {
    AIRPORT, BRIDGE, CITY, DUNES, FACTORY, FOREST, GRASS, HEADQUARTERS, LAB, MOUNTAIN, REEF, RIVER, ROAD, SEA, SEAPORT, SHOAL
  };

  @Override
  public String toString()
  {
    if( this == AIRPORT ) { return "AIRPORT"; }
    if( this == BRIDGE ) { return "BRIDGE"; }
    if( this == CITY ) { return "CITY"; }
    if( this == DUNES ) { return "DUNES"; }
    if( this == FACTORY ) { return "FACTORY"; }
    if( this == FOREST ) { return "FOREST"; }
    if( this == GRASS ) { return "GRASS"; }
    if( this == HEADQUARTERS ) { return "HEADQUARTERS"; }
    if( this == LAB ) { return "LAB"; }
    if( this == MOUNTAIN ) { return "MOUNTAIN"; }
    if( this == REEF ) { return "REEF"; }
    if( this == RIVER ) { return "RIVER"; }
    if( this == ROAD ) { return "ROAD"; }
    if( this == SEA ) { return "SEA"; }
    if( this == SEAPORT ) { return "SEAPORT"; }
    if( this == SHOAL ) { return "SHOAL"; }
    return "UNKNOWN";
  }
}
