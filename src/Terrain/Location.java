package Terrain;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Units.Unit;

public class Location
{
  private Environment environs = null;
  private Commander owner = null;
  private Unit resident = null;
  private final XYCoord coords;
  private boolean highlightSet = false;

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

  public void setOwner(Commander owner)
  {
    // remove ourselves from the previous owner's list, if one exists
    if( null != this.owner )
    {
      this.owner.ownedProperties.remove(this);
    }
    this.owner = owner;
    // add ourselves to the new owner's list, if it exists
    if( null != this.owner )
    {
      this.owner.ownedProperties.add(this);
    }
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
   * @return true if this Location has an ownable environment, false else.
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

  public void setHighlight(boolean val)
  {
    highlightSet = val;
  }

  public boolean isHighlightSet()
  {
    return highlightSet;
  }

  public Location(Environment environment, XYCoord coordinates)
  {
    environs = environment;
    owner = null;
    resident = null;
    coords = coordinates;
  }
}
