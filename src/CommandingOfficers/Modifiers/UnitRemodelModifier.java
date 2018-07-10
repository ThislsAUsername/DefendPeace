package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import Units.Unit;
import Units.UnitModel;

/** Modifier to temporarily turn one unit into another kind of unit.
 *  This only applies to active units (newly-built units will not be changed).
 *  There are definitely no plans to use this to make werewolves. */
public class UnitRemodelModifier implements COModifier
{
  private Map<UnitModel, UnitModel> modelSwaps = null;
  private Map<UnitModel, UnitModel> modelSwapBacks = null;
  private ArrayList<Unit> unitsChanged = null;

  public UnitRemodelModifier()
  {
    modelSwaps = new HashMap<UnitModel, UnitModel>();
    modelSwapBacks = new HashMap<UnitModel, UnitModel>();
    unitsChanged = new ArrayList<Unit>();
  }

  public UnitRemodelModifier(UnitModel oldModel, UnitModel newModel)
  {
    this();
    addUnitRemodel(oldModel, newModel);
  }

  /** Add a new unit transform assignment. */
  public void addUnitRemodel(UnitModel oldModel, UnitModel newModel)
  {
    modelSwaps.put(oldModel, newModel);
    modelSwapBacks.put(newModel, oldModel);
  }

  @Override
  public void apply(Commander commander)
  {
    for( Unit unit : commander.units )
    {
      if( modelSwaps.containsKey(unit.model) )
      {
        unit.model = modelSwaps.get(unit.model);
        unitsChanged.add(unit);
      }
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( Unit unit : unitsChanged )
    {
      if( modelSwapBacks.containsKey(unit.model) )
      {
        unit.model = modelSwapBacks.get(unit.model);
      }
    }
  }
}
