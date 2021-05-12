package Engine.GameEvents;

import java.util.ArrayList;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.MapLocation;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * This event changes the TerrainType and/or Weather of one or more map tiles.
 */
public class MapChangeEvent implements GameEvent
{
  private ArrayList<EnvironmentAssignment> changes;

  /** Simple constructor for changing a single tile */
  public MapChangeEvent(XYCoord location, Environment envi)
  {
    changes = new ArrayList<EnvironmentAssignment>();
    changes.add(new EnvironmentAssignment(location, envi));
  }

  /** Allows changing a set of tiles all at once. */
  public MapChangeEvent(ArrayList<EnvironmentAssignment> envChanges)
  {
    if( envChanges.size() < 1 )
      System.out.println("Warning: Generating a MapChangeEvent with no changes to the map.");
    changes = envChanges;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveTerrainChangeEvent(changes);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for( EnvironmentAssignment ea : changes )
    {
      MapLocation loc = gameMap.getLocation(ea.where);
      if( null != loc )
      {
        if( loc.getEnvironment().terrainType != ea.environment.terrainType )
          loc.durability = 99;
        loc.setEnvironment(ea.environment);
        if( ea.duration > 0 )
          loc.setForecast(ea.environment.weatherType, (gameMap.commanders.length * ea.duration) - 1);
      }
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    if( changes.size() > 0 )
      return changes.get(0).where;
    return null;
  }

  @Override
  public XYCoord getEndPoint()
  {
    if( changes.size() > 0 )
      return changes.get(0).where;
    return null;
  }

  public static class EnvironmentAssignment
  {
    public final XYCoord where;
    public final Environment environment;
    public final int duration;

    public EnvironmentAssignment(XYCoord xyc, Environment envi)
    {
      this(xyc, envi, 0);
    }

    /**
     * @param xyc The location whose Environment is changing.
     * @param envi The new TerrainType and weather to apply.
     * @param forecastDuration The number of rounds for the weather should persist.
     */
    public EnvironmentAssignment(XYCoord xyc, Environment envi, int forecastDuration)
    {
      where = xyc;
      environment = envi;
      duration = forecastDuration;
    }
  }
}
