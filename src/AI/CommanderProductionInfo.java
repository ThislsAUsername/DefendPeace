package AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Engine.Army;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 * Keeps track of a army's production facilities. When created, it will automatically catalog
 * all available facilities, and all units that can be built. It is then easy to ask whether it is
 * possible to build a given type of unit, or find a location to do so.
 * Once a purchase has been scheduled, removeBuildLocation() will remove a given facility from any
 * further consideration.
 */
public class CommanderProductionInfo
{
  public Army myArmy;
  public Set<UnitModel> availableUnitModels;
  public Set<MapLocation> availableProperties;
  public Map<Terrain.TerrainType, Integer> propertyCounts;
  public Map<UnitModel, Set<TerrainType>> modelToTerrainMap;

  /**
   * Build a model of the production capabilities for a given Commander.
   * Could be used for your own, or your opponent's.
   */
  public CommanderProductionInfo(Army co, GameMap gameMap, boolean includeFriendlyOccupied)
  {
    // Figure out what unit types we can purchase with our available properties.
    myArmy = co;
    availableUnitModels = new HashSet<UnitModel>();
    availableProperties = new HashSet<MapLocation>();
    propertyCounts = new HashMap<Terrain.TerrainType, Integer>();
    modelToTerrainMap = new HashMap<UnitModel, Set<TerrainType>>();

    for( XYCoord xyc : co.getOwnedProperties() )
    {
      MapLocation loc = co.myView.getLocation(xyc);
      Unit blocker = loc.getResident();
      if( null == blocker
          || (includeFriendlyOccupied && co == blocker.CO.army && !blocker.isTurnOver) )
      {
        ArrayList<UnitModel> models = loc.getOwner().getShoppingList(loc);
        availableUnitModels.addAll(models);
        availableProperties.add(loc);
        TerrainType terrain = loc.getEnvironment().terrainType;
        if( propertyCounts.containsKey(terrain))
        {
          propertyCounts.put(terrain, propertyCounts.get(loc.getEnvironment().terrainType)+1);
        }
        else
        {
          propertyCounts.put(terrain, 1);
        }

        // Store a mapping from UnitModel to the TerrainType that can produce it.
        for( UnitModel m : models )
        {
          if( modelToTerrainMap.get(m) == null )
            modelToTerrainMap.put(m, new HashSet<TerrainType>());
          modelToTerrainMap.get(m).add( loc.getEnvironment().terrainType );
        }
      }
    }
  }

  /**
   * Return a location that can build the given unitModel, or null if none remains.
   */
  public MapLocation getLocationToBuild(UnitModel model)
  {
    Set<TerrainType> desiredTerrains = modelToTerrainMap.get(model);
    MapLocation location = null;
    for( MapLocation loc : availableProperties )
    {
      if( desiredTerrains.contains(loc.getEnvironment().terrainType) )
      {
        location = loc;
        break;
      }
    }
    return location;
  }

  /**
   * Remove the given location from further consideration, even if it is still available.
   */
  public void removeBuildLocation(MapLocation loc)
  {
    availableProperties.remove(loc);
    TerrainType terrain = loc.getEnvironment().terrainType;
    if( propertyCounts.containsKey(terrain) )
    {
      propertyCounts.put(terrain, propertyCounts.get(terrain) - 1);
      if( propertyCounts.get(terrain) == 0 )
      {
        availableUnitModels.removeAll(loc.getOwner().getShoppingList(loc));
      }
    }
  }

  /**
   * Returns the number of facilities that can produce units of the given type.
   */
  public int getNumFacilitiesFor(UnitModel model)
  {
    int num = 0;
    if( modelToTerrainMap.containsKey(model) )
    {
      for( TerrainType terrain : modelToTerrainMap.get(model) )
      {
        num += propertyCounts.get(terrain);
      }
    }
    return num;
  }
}