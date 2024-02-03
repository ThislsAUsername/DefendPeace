package Terrain;

import java.util.ArrayDeque;
import java.io.Serializable;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.Environment.Weathers;
import Units.ITargetable;
import Units.Unit;
import Units.WeaponModel;

public class MapLocation implements Serializable, ITargetable
{
  private static final long serialVersionUID = 1L;
  private Environment environs = null;
  private Commander owner = null;
  private Unit resident = null;
  private final XYCoord coords;
  public int durability = 99;
  public ArrayDeque<Weathers> forecast = new ArrayDeque<>();

  public Environment getEnvironment()
  {
    return environs;
  }

  public void setEnvironment(Environment environment)
  {
    this.environs = environment;
  }
  
  public XYCoord getCoordinates()
  {
    return coords;
  }

  public Commander getOwner()
  {
    return owner;
  }

  /**
   * If the actual map state is changing, make sure to update the ownedProperties lists.<p>
   * MapMaster.setOwner() is a convenient way to do so.
   */
  public void setOwner(Commander owner)
  {
    this.owner = owner;
  }

  public Unit getResident()
  {
    return resident;
  }

  public void setResident(Unit resident)
  {
    this.resident = resident;
  }

  /**
   * @return true if this MapLocation has an ownable environment, false else.
   */
  public boolean isCaptureable()
  {
    return environs.terrainType.isCapturable();
  }

  /** Return whether the terrain type in this location can generate income. */
  public boolean isProfitable()
  {
    return environs.terrainType.isProfitable();
  }

  public MapLocation(Environment environment, XYCoord coordinates)
  {
    environs = environment;
    owner = null;
    resident = null;
    coords = coordinates;
  }
  
  public void setForecast(Weathers w, int duration)
  {
    setEnvironment(Environment.getTile(getEnvironment().terrainType, w));
    for( int turns = 0; turns < duration; turns++ )
    {
      forecast.pollFirst();
    }
    for( int turns = 0; turns < duration; turns++ )
    {
      forecast.addFirst(w);
    }
  }

  @Override
  public int getDamageRedirect(WeaponModel wm)
  {
    return wm.getDamage(environs.terrainType);
  }

  @Override
  public String toString()
  {
    return environs.terrainType.toString();
  }

  public String toStringWithLocation()
  {
    return String.format("%s at %s", toString(), coords);
  }
}
