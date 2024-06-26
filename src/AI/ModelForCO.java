package AI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import Engine.Army;
import Units.Unit;
import Units.UnitModel;
import lombok.Data;

/**
 * Co-locates a Commander and unit type to allow calculating CO-specific unit stats that aren't directly available from just the model.
 * <p>Useful to AIs, since they would like to track the capabilities of a particular unit type for a specific CO,
 *    so they can adapt to the unit matchups in *this* battle.
 */
@Data
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
  public String toString()
  {
    return um.name + " for " + co.coInfo.name;
  }
}
