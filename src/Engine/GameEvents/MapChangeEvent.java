package Engine.GameEvents;

import java.util.ArrayList;

import Engine.XYCoord;
import Terrain.Environment;
import Terrain.Location;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

/**
 * This event changes the TerrainType of one or more map tiles.
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
    changes = envChanges;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveTerrainChangeEvent(changes);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    for( EnvironmentAssignment ea : changes )
    {
      Location loc = gameMap.getLocation(ea.where);
      if( null != loc )
      {
        loc.setEnvironment(ea.environment);
        loc.setForecast(ea.environment.weatherType, ea.duration);
      }
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return changes.get(0).where;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return changes.get(0).where;
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
     * @param forecastDuration The duration over which the weather type will persist.
     */
    public EnvironmentAssignment(XYCoord xyc, Environment envi, int forecastDuration)
    {
      where = xyc;
      environment = envi;
      duration = forecastDuration;
    }
  }
}
