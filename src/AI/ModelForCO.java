package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import Engine.Army;
import Units.Unit;
import Units.UnitModel;

/**
 * Maps a particular CO to a unit type, since that's a useful construct for AIs
 */
public class ModelForCO implements Serializable
{
  private static final long serialVersionUID = -9123241409244613501L;
  public final Commander co;
  public final UnitModel um;

  public ModelForCO(Commander co, UnitModel um)
  {
    if( null == co || null == um )
      throw new NullPointerException();
    this.co = co;
    this.um = um;
  }
  public ModelForCO(Unit unit)
  {
    this(unit.CO, unit.model);
  }

  /**
   * @return A list of all the input CO's unit types
   */
  public static Collection<ModelForCO> getListFor(Commander co)
  {
    Collection<ModelForCO> models = new ArrayList<>();
    for( UnitModel um : co.unitModels )
      models.add(new ModelForCO(co, um));
    return models;
  }

  /**
   * @return A list of all the input Army's COs' unit types
   */
  public static Collection<ModelForCO> getListFor(Army army)
  {
    Collection<ModelForCO> models = new ArrayList<>();
    for( Commander co : army.cos )
      for( UnitModel um : co.unitModels )
        models.add(new ModelForCO(co, um));
    return models;
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + co.hashCode();
    result = prime * result + um.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if( this == obj )
      return true;
    if( obj == null )
      return false;
    if( getClass() != obj.getClass() )
      return false;
    ModelForCO other = (ModelForCO) obj;
    if( !co.equals(other.co) )
      return false;
    if( !um.equals(other.um) )
      return false;
    return true;
  }

  @Override
  public String toString()
  {
    return um.name + " for " + co.coInfo.name;
  }
}
