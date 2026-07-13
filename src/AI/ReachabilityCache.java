package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import Engine.GameInstance;
import Engine.PathCalcParams;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.Environment;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModelScheme.GameReadyModels;
import Units.MoveTypes.MoveType;
import lombok.var;

/**
 * Keeps track of where each unit type can get to on its own.
 * <p>Assumes that COs will not change what tiles are reachable by a unit type.
 */
// TODO: Recalculate when the map changes, or if a tile reachable by a Fey MoveType has changed hands.
// TODO: Consider calculating what terrain might need to be blown up/captured/built to join two islands.
public class ReachabilityCache implements Serializable
{
  private static final long serialVersionUID = 1L;

  /**
   * Caches mutually-reachable tiles for a specific movetype. A ship's island is made of water and ports.
   */
  public static class Island implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public final MoveType moveType; // The movetype we calculated with.
    public HashSet<XYCoord> coords = new HashSet<>(); // The meat of the Island.
    public HashSet<XYCoord> capturableCoords = new HashSet<>(); // The tasty bits.
    public HashSet<Island> overlapIslands = new HashSet<>(); // Islands with any overlap with this one (including itself). Does not necessarily imply load/unload is possible.
    public Island(MoveType mt)
    {
      moveType = mt;
    }
  }
  public HashSet<Island> allIslands = new HashSet<>();
  public Map<MoveType, ArrayList<Island>> islandSetsByMoveType = new HashMap<>();
  protected Map<MoveType, Island>[][] coordToIslandByMoveType;

  /**
   * May return null, to indicate the unit's current tile is not on an island.
   * <p>If that's the case, call getAdjacentIslands() instead to see if you have options.
   */
  public Island getIsland(Unit unit)
  {
    return getIsland(unit.model.baseMoveType, new XYCoord(unit));
  }
  public Island getIsland(MoveType mt, XYCoord xyc)
  {
    if (xyc.x < 0 || coordToIslandByMoveType.length <= xyc.x)
      return null;
    if (xyc.y < 0 || coordToIslandByMoveType[xyc.x].length <= xyc.y)
      return null;
    
    var islandByMoveType = coordToIslandByMoveType[xyc.x][xyc.y];
    return islandByMoveType.getOrDefault(mt, null);
  }

  public HashSet<Island> getAdjacentIslands(UnitContext uc, GameMap map)
  {
    return getAdjacentIslands(uc.model.baseMoveType, uc.coord, map);
  }
  public HashSet<Island> getAdjacentIslands(MoveType mt, XYCoord xyc, GameMap map)
  {
    HashSet<Island> islands = new HashSet<>();
    if (xyc.x < 0 || coordToIslandByMoveType.length <= xyc.x)
      return islands;
    if (xyc.y < 0 || coordToIslandByMoveType[xyc.x].length <= xyc.y)
      return islands;

    ArrayList<XYCoord> coordsToCheck = Utils.findLocationsInRange(map, xyc, 1, 1);
    for( XYCoord next : coordsToCheck )
    {
      var reachable = getIsland(mt, next);
      if (null != reachable)
        islands.add(reachable);
    }

    return islands;
  }

  public ReachabilityCache(GameInstance gi)
  {
    var ums = gi.rules.unitModelScheme;
    GameReadyModels grms = ums.getGameReadyModels();

    Map<MoveType, HashSet<MoveType>> transporterMovetypesByMoveType = new HashMap<>(); // Don't wanna delete the code that uses this yet
    for( var cargo : grms.unitModels )
    {
      if( islandSetsByMoveType.containsKey(cargo.baseMoveType) )
        continue;
      islandSetsByMoveType.put(cargo.baseMoveType, new ArrayList<>());
      transporterMovetypesByMoveType.put(cargo.baseMoveType, new HashSet<>());
    }

    for( var modelT : grms.unitModels )
    {
      if( modelT.baseCargoCapacity < 1 )
        continue;
      for( var cargo : grms.unitModels )
      {
        if( !modelT.isCargoRole(cargo.role) )
          continue;
        var cargoCarriers = transporterMovetypesByMoveType.get(cargo.baseMoveType);
        cargoCarriers.add(modelT.baseMoveType);
        transporterMovetypesByMoveType.put(cargo.baseMoveType, cargoCarriers);
      }
    }
    calcIslands(gi.gameMap);
  }

  @SuppressWarnings("unchecked") // coordToIslandByMoveType construction
  public void calcIslands(GameMap map)
  {
    allIslands.clear();
    for( MoveType mt : islandSetsByMoveType.keySet() )
      islandSetsByMoveType.get(mt).clear();
    coordToIslandByMoveType = new HashMap[map.mapWidth][map.mapHeight];
    for( int x = 0; x < map.mapWidth; x++ )
      for( int y = 0; y < map.mapHeight; y++ )
        coordToIslandByMoveType[x][y] = new HashMap<>();

    for( int x = 0; x < map.mapWidth; x++ )
    {
      for( int y = 0; y < map.mapHeight; y++ )
      {
        Map<MoveType, Island> coordIslandMap = coordToIslandByMoveType[x][y];
        XYCoord xyc     = new XYCoord(x, y);
        Environment env = map.getEnvironment(x, y);
        for( var mt : islandSetsByMoveType.keySet() ) // all unique movetypes
        {
          if( !mt.canStandOn(env) )
            continue;
          if( coordIslandMap.containsKey(mt) )
            continue; // Already part of an island
          PathCalcParams pcp = new PathCalcParams(mt, 3, xyc, map); // Chosen by a fair die roll. Guaranteed to be random.
          pcp.setTheoretical();
          var reachables = pcp.findAllPaths();

          var island = new Island(mt);
          for( var reached : reachables )
          {
            var islandXYC = new XYCoord(reached.x, reached.y); // Drop the extra SearchNode params to save a little memory.
            island.coords.add(islandXYC);
            if( map.getEnvironment(islandXYC).terrainType.isCapturable() )
              island.capturableCoords.add(islandXYC);
            coordToIslandByMoveType[islandXYC.x][islandXYC.y].put(mt, island);
          }
          islandSetsByMoveType.get(mt).add(island);
          allIslands.add(island);
        } // ~movetypes

        // Now that we have our islands, populate overlaps.
        for( var i : coordIslandMap.keySet() )
          for( var j : coordIslandMap.keySet() )
            coordIslandMap.get(i).overlapIslands.add(coordIslandMap.get(j));
      } // ~y
    } // ~x
  } // ~calcIslands

}
