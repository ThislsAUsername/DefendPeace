package AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import CommandingOfficers.Commander;
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
  public Set<ModelForCO> availableUnitModels;
  public Set<MapLocation> availableProperties;
  public Map<Commander, Map<Terrain.TerrainType, Integer>> propertyCounts;
  public Map<UnitModel, Set<TerrainType>> modelToTerrainMap;

  /**
   * Build a model of the production capabilities for a given Army.
   * Could be used for your own, or your opponent's.
   */
  public CommanderProductionInfo(Army army, GameMap gameMap, boolean includeFriendlyOccupied)
  {
    // Figure out what unit types we can purchase with our available properties.
    myArmy = army;
    availableUnitModels = new HashSet<>();
    availableProperties = new HashSet<>();
    propertyCounts = new HashMap<>();
    for( Commander co : army.cos )
      propertyCounts.put(co, new HashMap<>());
    modelToTerrainMap = new HashMap<>();

    for( XYCoord xyc : army.getOwnedProperties() )
    {
      MapLocation loc = army.myView.getLocation(xyc);
      Unit blocker = loc.getResident();
      if( null == blocker
          || (includeFriendlyOccupied && army == blocker.CO.army && !blocker.isTurnOver) )
      {
        ArrayList<UnitModel> models = loc.getOwner().getShoppingList(loc);
        Map<TerrainType, Integer> propsForCO = propertyCounts.get(loc.getOwner());
        availableProperties.add(loc);
        TerrainType terrain = loc.getEnvironment().terrainType;
        if( propsForCO.containsKey(terrain))
        {
          propsForCO.put(terrain, propsForCO.get(loc.getEnvironment().terrainType)+1);
        }
        else
        {
          propsForCO.put(terrain, 1);
        }

        // Store a mapping from UnitModel to the TerrainType that can produce it.
        for( UnitModel m : models )
        {
          availableUnitModels.add(new ModelForCO(loc.getOwner(), m));
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
    if( null == desiredTerrains )
      return null;
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
   * Return a location that can build the given unitModel, or null if none remains.
   */
  public MapLocation getLocationToBuild(ModelForCO model)
  {
    Set<TerrainType> desiredTerrains = modelToTerrainMap.get(model.um);
    if( null == desiredTerrains )
      return null;
    MapLocation location = null;
    for( MapLocation loc : availableProperties )
    {
      if( loc.getOwner() == model.co && desiredTerrains.contains(loc.getEnvironment().terrainType) )
      {
        location = loc;
        break;
      }
    }
    return location;
  }

  /**
   * Remove the given location from further consideration, even if it is still available.
   * NOTE: This assumes there is no overlap in what different property types can produce
   */
  public void removeBuildLocation(MapLocation loc)
  {
    final Commander owner = loc.getOwner();

    availableProperties.remove(loc);
    TerrainType terrain = loc.getEnvironment().terrainType;
    Map<TerrainType, Integer> propsForCO = propertyCounts.get(owner);
    if( propsForCO.containsKey(terrain) )
    {
      propsForCO.put(terrain, propsForCO.get(terrain) - 1);
      if( propsForCO.get(terrain) == 0 )
      {
        for( UnitModel um : owner.getShoppingList(loc) )
          availableUnitModels.remove(new ModelForCO(owner, um));
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
      for( Commander co : myArmy.cos )
      {
        Map<TerrainType, Integer> propsForCO = propertyCounts.get(co);
        for( TerrainType terrain : modelToTerrainMap.get(model) )
        {
          num += propsForCO.get(terrain);
        }
      }
    }
    return num;
  }

  /**
   * Returns the number of facilities that can produce units of the given type.
   */
  public int getNumFacilitiesFor(ModelForCO model)
  {
    int num = 0;
    if( modelToTerrainMap.containsKey(model.um) )
    {
      Map<TerrainType, Integer> propsForCO = propertyCounts.get(model.co);
      for( TerrainType terrain : modelToTerrainMap.get(model.um) )
      {
        if( propsForCO.containsKey(terrain) )
          num += propsForCO.get(terrain);
      }
    }
    return num;
  }

  /**
   * Returns the price per unit you would pay if you built the maximum number of units of the given type.
   */
  public int getAverageCostFor(UnitModel model)
  {
    Set<TerrainType> desiredTerrains = modelToTerrainMap.get(model);
    if( null == desiredTerrains )
      return 0;

    int num = 0;
    int totalCost = 0;
    for( MapLocation loc : availableProperties )
    {
      if( desiredTerrains.contains(loc.getEnvironment().terrainType) )
      {
        ++num;
        totalCost += loc.getOwner().getBuyCost(model, loc.getCoordinates());
      }
    }
    return totalCost / num;
  }

  /**
   * Returns the lowest price for this unit type available.
   */
  public int getMinCostFor(UnitModel model)
  {
    Set<TerrainType> desiredTerrains = modelToTerrainMap.get(model);
    if( null == desiredTerrains )
      return Integer.MAX_VALUE;

    int minCost = Integer.MAX_VALUE;
    for( MapLocation loc : availableProperties )
    {
      if( desiredTerrains.contains(loc.getEnvironment().terrainType) )
      {
        minCost = Math.min(minCost, loc.getOwner().getBuyCost(model, loc.getCoordinates()));
      }
    }
    return minCost;
  }
}