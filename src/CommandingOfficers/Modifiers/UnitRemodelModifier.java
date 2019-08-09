package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import Units.Weapons.Weapon;

/** Modifier to temporarily turn one unit into another kind of unit.
 *  This only applies to active units (newly-built units will not be changed).
 *  There are definitely no plans to use this to make werewolves. */
public class UnitRemodelModifier implements COModifier
{
  private static final long serialVersionUID = 1L;
  private Map<UnitEnum, UnitEnum> modelSwaps = null;
  private Map<Unit, UnitModel> modelSwapBacks = null;
  private ArrayList<Unit> unitsChanged = null;
  private Map<Unit, ArrayList<Weapon>> unitWeapons = null;

  public UnitRemodelModifier()
  {
    modelSwaps = new HashMap<UnitEnum, UnitEnum>();
    modelSwapBacks = new HashMap<Unit, UnitModel>();
    unitsChanged = new ArrayList<Unit>();
    unitWeapons = new HashMap<Unit, ArrayList<Weapon>>();
  }

  public UnitRemodelModifier(UnitEnum oldModel, UnitEnum newModel)
  {
    this();
    addUnitRemodel(oldModel, newModel);
  }

  /** Add a new unit transform assignment. */
  public void addUnitRemodel(UnitEnum oldModel, UnitEnum newModel)
  {
    modelSwaps.put(oldModel, newModel);
  }

  @Override
  public void applyChanges(Commander commander)
  {
    for( Unit unit : commander.units )
    {
      if( modelSwaps.containsKey(unit.model.type) )
      {
        // Store off the unit's weapons.
        unitWeapons.put(unit, unit.weapons);

        // Swap the unit's identity and store it for later.
        modelSwapBacks.put(unit, unit.model);
        unit.model = unit.CO.unitModels.get(modelSwaps.get(unit.model.type));

        // Change out weapons.
        doWeaponSwap(unit, unit.model);

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
      unit.weapons = unitWeapons.get(unit);
    }
  }

  private void doWeaponSwap(Unit unit, UnitModel model)
  {
    if( model.weaponModels != null )
    {
      unit.weapons = new ArrayList<Weapon>();
      for( int i = 0; i < model.weaponModels.size(); i++ )
      {
        unit.weapons.add(new Weapon(model.weaponModels.get(i)));
      }
    }
    else
    {
      // Just make sure we don't crash if we try to iterate on this.
      unit.weapons = new ArrayList<Weapon>();
    }
  }
}
