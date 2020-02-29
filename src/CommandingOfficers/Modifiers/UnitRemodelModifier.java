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
  private static final long serialVersionUID = 1L;
  private Map<UnitModel, UnitModel> modelSwaps = null;
  private Map<Unit, UnitModel> modelSwapBacks = null;
  private ArrayList<Unit> unitsChanged = null;

  public UnitRemodelModifier()
  {
    modelSwaps = new HashMap<UnitModel, UnitModel>();
    modelSwapBacks = new HashMap<Unit, UnitModel>();
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
  }

  @Override
  public void applyChanges(Commander commander)
  {
    for( Unit unit : commander.units )
    {
      if( modelSwaps.containsKey(unit.model) )
      {
        // Swap the unit's identity and store it for later.
        modelSwapBacks.put(unit, unit.model);
        unit.model = modelSwaps.get(unit.model);

        unitsChanged.add(unit);
      }
    }
  }

  @Override
  public void revertChanges(Commander commander)
  {
    for( Unit unit : unitsChanged )
    {
      unit.model = modelSwapBacks.get(unit);
    }
  }

}
