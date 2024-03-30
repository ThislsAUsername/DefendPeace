package Terrain;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import Engine.GameScenario.FogMode;

public class TerrainType implements Serializable
{
  // Local class data
  private int mDefenseLevel = -1;   // Level of protection provided by this terrain type. Typically 0-4.
  private int mAttributes = 0;      // bitmask of binary tile characteristics.
  private String mName;             // Human-readable name of the terrain type.
  private int mVisionBoost = 0;     // How much this terrain enhances the vision of surface units on it.
  private TerrainType mBase;        // What this terrain turns into if it's destroyed
  private int mCapThreshold = 20;   // How much capturing is needed to take ownership.

  // Generic constructor.
  private TerrainType(int attributeFlags, int defense, String name, TerrainType base)
  {
    this(attributeFlags, defense, name, base, 0, 20);
  }

  private TerrainType(int attributeFlags, int defense, String name, TerrainType base, int visionBoost)
  {
    this(attributeFlags, defense, name, base, visionBoost, 20);
  }

  private TerrainType(int attributeFlags, int defense, String name, TerrainType base, int visionBoost, int captureNeeded)
  {
    mAttributes = attributeFlags;
    mDefenseLevel = defense;
    mName = name;
    mVisionBoost = visionBoost;
    mBase = base;
    mCapThreshold = captureNeeded;
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  //// Protected constants to initialize/parse the attribute flags, and the methods to query them.
  private static final int LAND              = 1 << 0;
  private static final int WATER             = 1 << 1;
  private static final int CAPTURABLE        = 1 << 2; // Whether a Commander can take ownership of this property.
  private static final int PROFITABLE        = 1 << 3; // Whether this terrain type grants income to its owner each turn.
  private static final int PROVIDES_COVER    = 1 << 4; // Whether it hides surface units in fog
  private static final int HEALS_LAND        = 1 << 5;
  private static final int HEALS_SEA         = 1 << 6;
  private static final int HEALS_AIR         = 1 << 7;
  private static final int UNWEATHERABLE     = 1 << 8; // Not meaningfully affected by weather

  public int getDefLevel() { return mDefenseLevel; }
  public int getVisionBoost() { return mVisionBoost; }
  public int getCaptureThreshold() { return mCapThreshold; }
  public Boolean isCapturable() { return 0 != (mAttributes & CAPTURABLE); }
  public Boolean isProfitable() { return 0 != (mAttributes & PROFITABLE); }
  public Boolean isLand() { return 0 != (mAttributes & LAND); }
  public Boolean isWater() { return 0 != (mAttributes & WATER); }
  public Boolean healsLand() { return 0 != (mAttributes & HEALS_LAND); }
  public Boolean healsSea() { return 0 != (mAttributes & HEALS_SEA); }
  public Boolean healsAir() { return 0 != (mAttributes & HEALS_AIR); }
  public Boolean isUnweatherable() { return 0 != (mAttributes & UNWEATHERABLE); }
  public TerrainType getBaseTerrain() { return (null == mBase)? SEA : mBase; }

  public boolean isCover(FogMode mode)
  {
    if( 0 != (mAttributes & PROVIDES_COVER) )
      return true;
    // All capturables are cover in DoR logic
    if( mode.dorMode && 0 != (mAttributes & CAPTURABLE) )
      return true;
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////////////////////
  //// Publicly-accessible terrain Flyweight instances.
  private static final int SEA_FLAGS = WATER;
  private static final int SEA_DEFENSE = 0;
  private static final String SEA_NAME = "SEA";
  public static final TerrainType SEA = new TerrainType( SEA_FLAGS, SEA_DEFENSE, SEA_NAME, null );

  private static final int SHOAL_FLAGS = LAND | WATER;
  private static final int SHOAL_DEFENSE = 0;
  private static final String SHOAL_NAME = "SHOAL";
  public static final TerrainType SHOAL = new TerrainType( SHOAL_FLAGS, SHOAL_DEFENSE, SHOAL_NAME, SEA );

  private static final int GRASS_FLAGS = LAND;
  private static final int GRASS_DEFENSE = 1;
  private static final String GRASS_NAME = "GRASS";
  public static final TerrainType GRASS = new TerrainType( GRASS_FLAGS, GRASS_DEFENSE, GRASS_NAME, SHOAL );

  private static final int TEMP_AIRPORT_FLAGS = LAND | CAPTURABLE | HEALS_AIR;
  private static final int TEMP_AIRPORT_DEFENSE = 1;
  private static final String TEMP_AIRPORT_NAME = "TEMP_AIRPORT";
  public static final TerrainType TEMP_AIRPORT = new TerrainType( TEMP_AIRPORT_FLAGS, TEMP_AIRPORT_DEFENSE, TEMP_AIRPORT_NAME, GRASS );

  private static final int BRIDGE_FLAGS = LAND;
  private static final int BRIDGE_DEFENSE = 0;
  private static final String BRIDGE_NAME = "BRIDGE";
  public static final TerrainType BRIDGE = new TerrainType( BRIDGE_FLAGS, BRIDGE_DEFENSE, BRIDGE_NAME, SEA );

  private static final int CITY_FLAGS = LAND | CAPTURABLE | PROFITABLE | HEALS_LAND;
  private static final int CITY_DEFENSE = 3;
  private static final String CITY_NAME = "CITY";
  public static final TerrainType CITY = new TerrainType( CITY_FLAGS, CITY_DEFENSE, CITY_NAME, GRASS );

  private static final int DS_TOWER_FLAGS = LAND | CAPTURABLE | PROVIDES_COVER;
  private static final int DS_TOWER_DEFENSE = 3;
  private static final String DS_TOWER_NAME = "DS_TOWER";
  public static final TerrainType DS_TOWER = new TerrainType(DS_TOWER_FLAGS, DS_TOWER_DEFENSE, DS_TOWER_NAME, GRASS);

  private static final int DOR_TOWER_FLAGS = LAND | CAPTURABLE | PROVIDES_COVER | PROFITABLE;
  private static final int DOR_TOWER_DEFENSE = 3;
  private static final String DOR_TOWER_NAME = "DOR_TOWER";
  public static final TerrainType DOR_TOWER = new TerrainType(DOR_TOWER_FLAGS, DOR_TOWER_DEFENSE, DOR_TOWER_NAME, GRASS);

  private static final int BUNKER_FLAGS = LAND;
  private static final int BUNKER_DEFENSE = 3;
  private static final String BUNKER_NAME = "BUNKER";
  public static final TerrainType BUNKER = new TerrainType( BUNKER_FLAGS, BUNKER_DEFENSE, BUNKER_NAME, GRASS );

  private static final int AIRPORT_FLAGS = LAND | CAPTURABLE | PROFITABLE | HEALS_AIR;
  private static final int AIRPORT_DEFENSE = 3;
  private static final String AIRPORT_NAME = "AIRPORT";
  public static final TerrainType AIRPORT = new TerrainType( AIRPORT_FLAGS, AIRPORT_DEFENSE, AIRPORT_NAME, GRASS );

  private static final int PILLAR_FLAGS = UNWEATHERABLE;
  private static final int PILLAR_DEFENSE = 0;
  private static final String PILLAR_NAME = "PILLAR";
  public static final TerrainType PILLAR = new TerrainType( PILLAR_FLAGS, PILLAR_DEFENSE, PILLAR_NAME, GRASS );

  private static final int METEOR_FLAGS = UNWEATHERABLE;
  private static final int METEOR_DEFENSE = 0;
  private static final String METEOR_NAME = "METEOR";
  public static final TerrainType METEOR = new TerrainType( METEOR_FLAGS, METEOR_DEFENSE, METEOR_NAME, GRASS );

  private static final int DUNES_FLAGS = LAND;
  private static final int DUNES_DEFENSE = 1;
  private static final String DUNES_NAME = "DUNES";
  public static final TerrainType DUNES = new TerrainType( DUNES_FLAGS, DUNES_DEFENSE, DUNES_NAME, SHOAL );

  private static final int FACTORY_FLAGS = LAND | CAPTURABLE | PROFITABLE | HEALS_LAND;
  private static final int FACTORY_DEFENSE = 3;
  private static final String FACTORY_NAME = "FACTORY";
  public static final TerrainType FACTORY = new TerrainType( FACTORY_FLAGS, FACTORY_DEFENSE, FACTORY_NAME, GRASS );

  private static final int FOREST_FLAGS = LAND | PROVIDES_COVER;
  private static final int FOREST_DEFENSE = 2;
  private static final String FOREST_NAME = "FOREST";
  public static final TerrainType FOREST = new TerrainType( FOREST_FLAGS, FOREST_DEFENSE, FOREST_NAME, GRASS );

  private static final int HEADQUARTERS_FLAGS = LAND | CAPTURABLE | PROFITABLE | HEALS_LAND;
  private static final int HEADQUARTERS_DEFENSE = 4;
  private static final String HEADQUARTERS_NAME = "HEADQUARTERS";
  public static final TerrainType HEADQUARTERS = new TerrainType( HEADQUARTERS_FLAGS, HEADQUARTERS_DEFENSE, HEADQUARTERS_NAME, GRASS );

  private static final int LAB_FLAGS = LAND | CAPTURABLE;
  private static final int LAB_DEFENSE = 3;
  private static final String LAB_NAME = "LAB";
  public static final TerrainType LAB = new TerrainType( LAB_FLAGS, LAB_DEFENSE, LAB_NAME, GRASS );

  private static final int MOUNTAIN_FLAGS = LAND;
  private static final int MOUNTAIN_DEFENSE = 4;
  private static final String MOUNTAIN_NAME = "MOUNTAIN";
  private static final int MOUNTAIN_VISION  = 3;
  public static final TerrainType MOUNTAIN = new TerrainType( MOUNTAIN_FLAGS, MOUNTAIN_DEFENSE, MOUNTAIN_NAME, GRASS, MOUNTAIN_VISION );

  private static final int REEF_FLAGS = WATER | PROVIDES_COVER;
  private static final int REEF_DEFENSE = 1;
  private static final String REEF_NAME = "REEF";
  public static final TerrainType REEF = new TerrainType( REEF_FLAGS, REEF_DEFENSE, REEF_NAME, SEA );

  private static final int RIVER_FLAGS = LAND; // It's not really a WATER type, since boats can't go here.
  private static final int RIVER_DEFENSE = 0;
  private static final String RIVER_NAME = "RIVER";
  public static final TerrainType RIVER = new TerrainType( RIVER_FLAGS, RIVER_DEFENSE, RIVER_NAME, SEA );

  private static final int ROAD_FLAGS = LAND;
  private static final int ROAD_DEFENSE = 0;
  private static final String ROAD_NAME = "ROAD";
  public static final TerrainType ROAD = new TerrainType( ROAD_FLAGS, ROAD_DEFENSE, ROAD_NAME, SHOAL );

  private static final int TELETILE_FLAGS = UNWEATHERABLE;
  private static final int TELETILE_DEFENSE = 0;
  private static final String TELETILE_NAME = "TELETILE";
  public static final TerrainType TELETILE = new TerrainType( TELETILE_FLAGS, TELETILE_DEFENSE, TELETILE_NAME, null );

  private static final int SEAPORT_FLAGS = LAND | WATER | CAPTURABLE | PROFITABLE | HEALS_SEA;
  private static final int SEAPORT_DEFENSE = 3;
  private static final String SEAPORT_NAME = "SEAPORT";
  public static final TerrainType SEAPORT = new TerrainType( SEAPORT_FLAGS, SEAPORT_DEFENSE, SEAPORT_NAME, SHOAL );

  private static final int TEMP_SEAPORT_FLAGS = LAND | WATER | CAPTURABLE | HEALS_SEA;
  private static final int TEMP_SEAPORT_DEFENSE = 1;
  private static final String TEMP_SEAPORT_NAME = "TEMP_SEAPORT";
  public static final TerrainType TEMP_SEAPORT = new TerrainType( TEMP_SEAPORT_FLAGS, TEMP_SEAPORT_DEFENSE, TEMP_SEAPORT_NAME, SHOAL );


  // List of all terrain types.
  public static final ArrayList<TerrainType> TerrainTypeList =
      new ArrayList<TerrainType>(Arrays.asList(
          AIRPORT, TEMP_AIRPORT, BRIDGE, CITY, DS_TOWER, DOR_TOWER, BUNKER, PILLAR, METEOR, DUNES, FACTORY, FOREST, GRASS, HEADQUARTERS, LAB, MOUNTAIN, REEF, RIVER, ROAD, SEA, SEAPORT, TEMP_SEAPORT, SHOAL, TELETILE
          ));

  @Override
  public String toString()
  {
    return mName;
  }

  /**
   * Private method, same signature as in Serializable interface
   * 
   * This method lets us write a different kind of object out to the file, replacing ourselves.
   * This lets us retrieve a static TerrainType instance with the written object's readResolve().
   */
  private Object writeReplace() throws ObjectStreamException
  {
    return new SerialTerrain(TerrainTypeList.indexOf(this));
  }

  private static class SerialTerrain implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public int index;

    public SerialTerrain(int i)
    {
      index = i;
    }

    /**
     * Private method, same signature as in Serializable interface
     * 
     * This method lets us return a different object than we initially wrote to the file.
     */
    private Object readResolve() throws ObjectStreamException
    {
      return TerrainTypeList.get(index);
    }
  }
}
