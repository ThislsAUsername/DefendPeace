package AI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
   * Note that this only does a 1-ply search from the <b>destination</b>, so a recursive doughnut island would need extra scaffolding.
   */
  public static HashMap<UnitModel, HashSet<XYCoord>> findUnloadTiles(GameMap map, ReachabilityCache rc, UnitContext cargo, XYCoord dest)
  {
    var transportToBeachMap = new HashMap<UnitModel, HashSet<XYCoord>>();
    var grms = map.game.rules.unitModelScheme.getGameReadyModels();
    var transportTypes = new ArrayList<UnitModel>();

    for( var modelT : grms.unitModels )
    {
      if( modelT.baseCargoCapacity < 1 )
        continue;
      if( !modelT.isCargoRole(cargo.model.role) )
        continue;
      transportTypes.add(modelT);
    }

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
//    PathCalcParams pcp = new PathCalcParams(uc, gameMap);
//    pcp.start = origin;
//    pcp.includeOccupiedSpaces = true; // We assume the enemy knows how to manage positioning within his turn
//    ArrayList<SearchNode> destinations = pcp.findAllPaths();

}
