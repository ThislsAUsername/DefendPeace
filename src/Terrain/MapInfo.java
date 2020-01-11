package Terrain;

import java.util.ArrayList;
import java.util.Map;

import Engine.XYCoord;
import Units.AWBWUnits;
import Units.UnitModelScheme;

public class MapInfo
{
  public final String mapName;
  public final TerrainType[][] terrain;
  public final XYCoord[][] COProperties;
  public final ArrayList<Map<XYCoord,String>> mapUnits;

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props)
  {
    this(name, tiles, props, new ArrayList<Map<XYCoord,String>>());
  }

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props, ArrayList<Map<XYCoord,String>> units)
  {
    mapName = name;
    terrain = tiles;
    COProperties = props;
    mapUnits = units;
  }

  public int getWidth()
  {
    return terrain.length;
  }

  public int getHeight()
  {
    return terrain[0].length;
  }

  public int getNumCos()
  {
    return COProperties.length;
  }

  /**
   * Determines which unit sets are valid for this map.
   */
  public UnitModelScheme[] getValidUnitModelSchemes()
  {
    ArrayList<UnitModelScheme> umsList = new ArrayList<UnitModelScheme>();
    umsList.add(new AWBWUnits());
    
    // TODO: Filter based on the existence of non-core units in the map
    
    return umsList.toArray(new UnitModelScheme[0]);
  }
}
