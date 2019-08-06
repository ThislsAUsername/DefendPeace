package Terrain;

import java.awt.Color;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class TerrainType implements Serializable
{
  // Local class data
  private int mDefenseLevel = -1;   // Level of protection provided by this terrain type. Typically 0-4.
  private Color mMainColor = null;  // Predominant color of this terrain type. Here for convenience.
  private int mAttributes = 0;      // bitmask of binary tile characteristics.
  private String mName;             // Human-readable name of the terrain type.
  private int mVisionBoost = 0;     // How much this terrain enhances the vision of surface units on it.

  // Generic constructor.
  private TerrainType(int attributeFlags, int defense, Color mainColor, String name)
  {
    this(attributeFlags, defense, mainColor, name, 0);
  }
  
  private TerrainType(int attributeFlags, int defense, Color mainColor, String name, int visionBoost)
  {
    mAttributes = attributeFlags;
    mDefenseLevel = defense;
    mMainColor = mainColor;
    mName = name;
    mVisionBoost = visionBoost;
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
  public int getVisionBoost() { return mVisionBoost; }
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
  private static final String AIRPORT_NAME = "AIRPORT";
  public static final TerrainType AIRPORT = new TerrainType( AIRPORT_FLAGS, AIRPORT_DEFENSE, AIRPORT_COLOR, AIRPORT_NAME);

  private static final int BRIDGE_FLAGS = LAND | WATER;
  private static final int BRIDGE_DEFENSE = 0;
  private static final Color BRIDGE_COLOR = new Color(189, 189, 189);
  private static final String BRIDGE_NAME = "BRIDGE";
  public static final TerrainType BRIDGE = new TerrainType( BRIDGE_FLAGS, BRIDGE_DEFENSE, BRIDGE_COLOR, BRIDGE_NAME );

  private static final int CITY_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static final int CITY_DEFENSE = 2;
  private static final Color CITY_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  private static final String CITY_NAME = "CITY";
  public static final TerrainType CITY = new TerrainType( CITY_FLAGS, CITY_DEFENSE, CITY_COLOR, CITY_NAME );
  
  private static final int BUNKER_FLAGS = LAND;
  private static final int BUNKER_DEFENSE = 3;
  private static final Color BUNKER_COLOR = new Color(155, 155, 255);
  private static final String BUNKER_NAME = "BUNKER";
  public static final TerrainType BUNKER = new TerrainType( BUNKER_FLAGS, BUNKER_DEFENSE, BUNKER_COLOR, BUNKER_NAME );
  
  private static final int PILLAR_FLAGS = 0;
  private static final int PILLAR_DEFENSE = 0;
  private static final Color PILLAR_COLOR = new Color(144, 104, 120);
  private static final String PILLAR_NAME = "PILLAR";
  public static final TerrainType PILLAR = new TerrainType( PILLAR_FLAGS, PILLAR_DEFENSE, PILLAR_COLOR, PILLAR_NAME );

  private static final int DUNES_FLAGS = LAND;
  private static final int DUNES_DEFENSE = 1;
  private static final Color DUNES_COLOR = new Color(240, 210, 120);
  private static final String DUNES_NAME = "DUNES";
  public static final TerrainType DUNES = new TerrainType( DUNES_FLAGS, DUNES_DEFENSE, DUNES_COLOR, DUNES_NAME );

  private static final int FACTORY_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static final int FACTORY_DEFENSE = 3;
  private static final Color FACTORY_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  private static final String FACTORY_NAME = "FACTORY";
  public static final TerrainType FACTORY = new TerrainType( FACTORY_FLAGS, FACTORY_DEFENSE, FACTORY_COLOR, FACTORY_NAME );

  private static final int FOREST_FLAGS = LAND | PROVIDES_COVER;
  private static final int FOREST_DEFENSE = 3;
  private static final Color FOREST_COLOR = new Color(46, 196, 24);
  private static final String FOREST_NAME = "FOREST";
  public static final TerrainType FOREST = new TerrainType( FOREST_FLAGS, FOREST_DEFENSE, FOREST_COLOR, FOREST_NAME );

  private static final int GRASS_FLAGS = LAND;
  private static final int GRASS_DEFENSE = 1;
  private static final Color GRASS_COLOR = new Color(166, 253, 77);
  private static final String GRASS_NAME = "GRASS";
  public static final TerrainType GRASS = new TerrainType( GRASS_FLAGS, GRASS_DEFENSE, GRASS_COLOR, GRASS_NAME );

  private static final int HEADQUARTERS_FLAGS = LAND | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_LAND;
  private static final int HEADQUARTERS_DEFENSE = 4;
  private static final Color HEADQUARTERS_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  private static final String HEADQUARTERS_NAME = "HEADQUARTERS";
  public static final TerrainType HEADQUARTERS = new TerrainType( HEADQUARTERS_FLAGS, HEADQUARTERS_DEFENSE, HEADQUARTERS_COLOR, HEADQUARTERS_NAME );

  private static final int LAB_FLAGS = LAND | CAPTURABLE | PROVIDES_COVER;
  private static final int LAB_DEFENSE = 3;
  private static final Color LAB_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  private static final String LAB_NAME = "LAB";
  public static final TerrainType LAB = new TerrainType( LAB_FLAGS, LAB_DEFENSE, LAB_COLOR, LAB_NAME );

  private static final int MOUNTAIN_FLAGS = LAND;
  private static final int MOUNTAIN_DEFENSE = 4;
  private static final Color MOUNTAIN_COLOR = new Color(153, 99, 67);
  private static final String MOUNTAIN_NAME = "MOUNTAIN";
  private static final int MOUNTAIN_VISION  = 3;
  public static final TerrainType MOUNTAIN = new TerrainType( MOUNTAIN_FLAGS, MOUNTAIN_DEFENSE, MOUNTAIN_COLOR, MOUNTAIN_NAME, MOUNTAIN_VISION );

  private static final int REEF_FLAGS = WATER | PROVIDES_COVER;
  private static final int REEF_DEFENSE = 2;
  private static final Color REEF_COLOR = new Color(218, 152, 112);
  private static final String REEF_NAME = "REEF";
  public static final TerrainType REEF = new TerrainType( REEF_FLAGS, REEF_DEFENSE, REEF_COLOR, REEF_NAME );

  private static final int RIVER_FLAGS = LAND; // It's not really a WATER type, since boats can't go here.
  private static final int RIVER_DEFENSE = 0;
  private static final Color RIVER_COLOR = new Color(148, 219, 255);
  private static final String RIVER_NAME = "RIVER";
  public static final TerrainType RIVER = new TerrainType( RIVER_FLAGS, RIVER_DEFENSE, RIVER_COLOR, RIVER_NAME );

  private static final int ROAD_FLAGS = LAND;
  private static final int ROAD_DEFENSE = 0;
  private static final Color ROAD_COLOR = new Color(189, 189, 189);
  private static final String ROAD_NAME = "ROAD";
  public static final TerrainType ROAD = new TerrainType( ROAD_FLAGS, ROAD_DEFENSE, ROAD_COLOR, ROAD_NAME );

  private static final int SEA_FLAGS = WATER;
  private static final int SEA_DEFENSE = 0;
  private static final Color SEA_COLOR = new Color(94, 184, 236);
  private static final String SEA_NAME = "SEA";
  public static final TerrainType SEA = new TerrainType( SEA_FLAGS, SEA_DEFENSE, SEA_COLOR, SEA_NAME );

  private static final int SEAPORT_FLAGS = LAND | WATER | CAPTURABLE | PROFITABLE | PROVIDES_COVER | HEALS_SEA;
  private static final int SEAPORT_DEFENSE = 3;
  private static final Color SEAPORT_COLOR = new Color(125, 125, 125); // TODO: define unique color for each building type?
  private static final String SEAPORT_NAME = "SEAPORT";
  public static final TerrainType SEAPORT = new TerrainType( SEAPORT_FLAGS, SEAPORT_DEFENSE, SEAPORT_COLOR, SEAPORT_NAME );

  private static final int SHOAL_FLAGS = LAND | WATER;
  private static final int SHOAL_DEFENSE = 0;
  private static final Color SHOAL_COLOR = new Color(253, 224, 93);
  private static final String SHOAL_NAME = "SHOAL";
  public static final TerrainType SHOAL = new TerrainType( SHOAL_FLAGS, SHOAL_DEFENSE, SHOAL_COLOR, SHOAL_NAME );

  // List of all terrain types.
  public static final ArrayList<TerrainType> TerrainTypeList =
      new ArrayList<TerrainType>(Arrays.asList(
          AIRPORT, BRIDGE, CITY, BUNKER, PILLAR, DUNES, FACTORY, FOREST, GRASS, HEADQUARTERS, LAB, MOUNTAIN, REEF, RIVER, ROAD, SEA, SEAPORT, SHOAL
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
