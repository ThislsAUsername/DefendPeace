package AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Engine.PathCalcParams;
import Engine.PathCalcParams.SearchNode;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.GameMap;
import Units.UnitContext;
import Units.UnitModel;
import lombok.var;

public class AITransportUtils
{
  /**
   * Build a map of potential transport types, and where they could unload the unit to reach its destination.<p>
   * Note that this only does a 1-ply search from the <b>destination</b>, so a recursive doughnut island would need extra scaffolding.<p>
   * Note also that this will not calc transports with the exact same movetype as the thing they're transporting, since they use the same island.
   */
  public static HashMap<UnitModel, HashSet<XYCoord>> findUnloadTiles(GameMap map, ReachabilityCache rc, UnitContext cargo, XYCoord dest)
  {
    var transportToBeachMap = new HashMap<UnitModel, HashSet<XYCoord>>();
    var grms = map.game.rules.unitModelScheme.getGameReadyModels();
    var transportTypes = new ArrayList<UnitModel>();

    // Grab relevant transport types for this cargo.
    for( var modelT : grms.unitModels )
    {
      if( modelT.baseCargoCapacity < 1 )
        continue;
      if( !modelT.canTransport(cargo.model.role) )
        continue;
      transportTypes.add(modelT);
    }

    // Figure out the tiles this transport could drop off dudes from.
    for( var modelT : transportTypes )
    {
      var destIsland = rc.getIsland(cargo.model.baseMoveType, dest);
      var beaches = new HashSet<XYCoord>();
      for( var connectedIsland : destIsland.overlapIslands )
      {
        if( connectedIsland.moveType != modelT.baseMoveType )
          continue;
        // Note that there could be multiple islands this transport could approach on.
        for( var xyc : connectedIsland.coords )
        {
          if( !destIsland.coords.contains(xyc) )
            continue; // Cargo mobility on the transport's terrain (i.e. island overlap) is required to unload
          var env = map.getEnvironment(xyc);
          if( modelT.unloadExclusionTerrain.contains(env.terrainType) )
            continue;
          ArrayList<XYCoord> dropoffLocations = Utils.findUnloadLocations(map, null, xyc, cargo.model.baseMoveType);
          if( !dropoffLocations.isEmpty() )
            beaches.add(xyc);
        }
      }

      transportToBeachMap.put(modelT, beaches);
    }

    return transportToBeachMap;
  }

  public static class InterceptPaths
  {
    SearchNode transport, cargo;
  }

  /**
   * Determines the places that cargo can load into transport with optimal turn usage (accounting for both units' turn status).
   * <p>This assumes cargo should load into transport while transport is still ready to act, if possible.
   * <p>If a UC's unit pointer is not populated, this function will assume the unit is to be built (on the UC coordinate).
   */
  // TODO: Consider enabling PCP to take in a list of start tiles, so less needs to be recalculated on each iteration?
  // Note that this only finds the soonest pickup locations, which may not optimize the overall trip. For a simple counterexample:
  // land   land  goal/beach
  // lander water water
  // beach  hill  beach
  // land   land  tank
  // Let's say tank can get anywhere on its landmass in one turn, but lander moves 2 tiles/turn
  // Tank can load in the left beach immediately, but this leads to the lander spending more time traveling and a later overall arrival date.
  // I have no idea how to make such a global optimization computationally tractable, though.
  // There are a number of potential cost types, too, so it seems like not the realm of a util function unless it gives you a BFS accounting of all optimal options?
  public static HashMap<XYCoord, InterceptPaths> findFirstLoadIntercepts(GameMap map, ReachabilityCache rc, UnitContext transport, UnitContext cargo)
  {
    HashMap<XYCoord, InterceptPaths> result = new HashMap<>();
    var cargoIslands = rc.getAdjacentIslands(cargo, map);
    if( cargoIslands.isEmpty() )
      return result; // Cargo can't move, so GGs

    boolean canLoad = false;
    if( cargoIslands.contains(rc.getIsland(cargo.model.baseMoveType, transport.coord)) )
      canLoad = true; // Cargo can reach the transport, even if the transport can't move.
    if( !canLoad )
    {
      var transportIslands = rc.getAdjacentIslands(transport, map);
      for( var cis : cargoIslands )
        for( var tis : transportIslands )
          if( cis.overlapIslands.contains(tis) )
          {
            canLoad = true; // Cargo can reach a place the transport can
            break;
          }
    }
    if( !canLoad )
      return result;

    int turnsCargo     = 0;
    int turnsTransport = 0;
    // We want the cargo to load while the transport has its turn.
    // This means cargo gets +1 turn advantage if equal initiative, +2 if it has TA, or +0 if it's waited and transport is not
    if( !cargo.isTurnOver && null != cargo.unit )
      turnsCargo += 1;
    if( transport.isTurnOver || null == cargo.unit )
      turnsCargo += 1;

    // Expand the units' reachable area 1 turn at a time, until they overlap on a loadable tile.
    PathCalcParams pcpCargo = new PathCalcParams(cargo, map);
    PathCalcParams pcpTransport = new PathCalcParams(transport, map);
    // I think accounting for current enemy blocking doesn't make a lot of sense for multi-turn calcs.
    // If I can already load this turn, I shouldn't be calling this function.
    // The only other scenario this calc would be important for is SFW-style transports, but I don't plan to add that because they're just bad in a not-fun way.
    pcpCargo.    canTravelThroughEnemies = true;
    pcpTransport.canTravelThroughEnemies = true;
    while( turnsCargo < 10 ) // just so it doesn't go infinite
    {
      pcpCargo.    maxTurns = turnsCargo;
      pcpTransport.maxTurns = turnsTransport;
      ArrayList<SearchNode> destinationsCargo     = pcpCargo.findAllPaths();
      ArrayList<SearchNode> destinationsTransport = pcpTransport.findAllPaths();
      for( var snC : destinationsCargo )
      {
        for( var snT : destinationsTransport )
        {
          if( !snC.equals(snT) )
            continue; // Doing a nested for loop instead of .contains() for convenience.
          var env = map.getEnvironment(snC);
          if( transport.model.unloadExclusionTerrain.contains(env.terrainType) )
            continue;
          InterceptPaths intercept = new InterceptPaths();
          intercept.cargo     = snC;
          intercept.transport = snT;
          result.put(snC, intercept);
        }
      }
      if( !result.isEmpty() )
        break;
      ++turnsCargo;
      ++turnsTransport;
    }

    return result;
  }

  /**
   * Calculates the set of paths from a pickup tile to an unload tile that takes the same (minimum) turn count for the transport.
   */
  public static HashSet<SearchNode> findShortestUnloadTrips(GameMap map, UnitContext transport, HashMap<XYCoord, InterceptPaths> pickups, HashSet<XYCoord> drops)
  {
    var result = new HashSet<SearchNode>();

    // We use extraStarts here because we don't know which load tile will give us the shortest trip, and this allows calculating that in one shot.
    PathCalcParams pcp = new PathCalcParams(transport, map);
    pcp.canTravelThroughEnemies = true;
    for( var p : pickups.keySet() )
    {
      pcp.start = p; // Arbitrary; just make sure we don't path from the transport's current position.
      pcp.extraStarts.add(pickups.get(p).transport); // It might be relevant to consider the data in the actual SearchNode at some point, in case pickups spans multiple turn options, but this problem is already too hard.
    }

    // Add turns until we reach an unload tile.
    for( int i = 0; i < 10; ++i )
    {
      pcp.maxTurns = 1 + i;
      ArrayList<SearchNode> destinations = pcp.findAllPaths();
      for( var dest : destinations )
      {
        if( !drops.contains(dest) )
          continue;
        result.add(dest);
      }
      if( !result.isEmpty() )
        break;
    }

    return result;
  }

}
