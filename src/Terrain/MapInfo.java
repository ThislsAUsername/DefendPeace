package Terrain;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import Engine.XYCoord;
import Terrain.Environment.Weathers;
import Units.AWBWUnits;
import Units.DoRUnits;
import Units.KaijuWarsUnits;
import Units.UnitModel;
import Units.UnitModelScheme;
import Units.UnitModelScheme.GameReadyModels;

public class MapInfo implements IEnvironsProvider
{
  public final String mapName;
  public final TerrainType[][] terrain;
  // Array of coordinates for properties owned by each player; the first index is the CO, the second is an arbitrary ordering
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
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  @Override
  public boolean isLocationValid(XYCoord coords)
  {
    return (coords != null) && isLocationValid(coords.x, coords.y);
  }
  /**
   * Returns true if (x,y) lies within the GameMap, false else.
   */
  @Override
  public boolean isLocationValid(int x, int y)
  {
    return !(x < 0 || x >= getWidth() || y < 0 || y >= getHeight());
  }

  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  @Override
  public Environment getEnvironment(XYCoord coord)
  {
    return getEnvironment(coord.x, coord.y);
  }
  /** Returns the Environment of the specified tile, or null if that location does not exist. */
  @Override
  public Environment getEnvironment(int x, int y)
  {
    if( !isLocationValid(x, y) )
    {
      return null;
    }
    return Environment.getTile(terrain[x][y], Weathers.CLEAR);
  }

  /**
   * Determines which unit sets are valid for this map.
   */
  public UnitModelScheme[] getValidUnitModelSchemes()
  {
    ArrayList<UnitModelScheme> umsList = new ArrayList<UnitModelScheme>();
    umsList.add(new AWBWUnits());
    umsList.add(new DoRUnits());
    umsList.add(new KaijuWarsUnits());

    // Filter based on the existence of non-core units in the map
    for( UnitModelScheme scheme : umsList )
    {
      GameReadyModels models = scheme.getGameReadyModels();
      boolean schemeInvalid = false;
      
      for( Map<XYCoord, String> unitSet : mapUnits )
      {
        for( Entry<XYCoord, String> unitEntry : unitSet.entrySet() )
        {
          UnitModel model = UnitModelScheme.getModelFromString(unitEntry.getValue(), models.unitModels);
          if( model == null )
          {
            schemeInvalid = true; // This unit isn't supported in the scheme, so let the scheme know it's deprecated for this map
            break;
          }
        }
        if( schemeInvalid )
          break;
      }

      scheme.schemeValid = !schemeInvalid;
    }

    return umsList.toArray(new UnitModelScheme[0]);
  }
}
