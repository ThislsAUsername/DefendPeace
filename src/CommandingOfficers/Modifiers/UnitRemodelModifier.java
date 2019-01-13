package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;

/** Modifier to temporarily turn one unit into another kind of unit.
 *  This only applies to active units (newly-built units will not be changed).
 *  There are definitely no plans to use this to make werewolves. */
public class UnitRemodelModifier implements COModifier
{
  protected Map<UnitModel, UnitModel> modelSwaps = null;
  protected Map<Unit, UnitModel> modelSwapBacks = null;
  protected ArrayList<Unit> unitsChanged = null;
  protected Map<Unit, ArrayList<Weapon>> unitWeapons = null;

  public UnitRemodelModifier()
  {
    modelSwaps = new HashMap<UnitModel, UnitModel>();
    modelSwapBacks = new HashMap<Unit, UnitModel>();
    unitsChanged = new ArrayList<Unit>();
    unitWeapons = new HashMap<Unit, ArrayList<Weapon>>();
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
  public void apply(Commander commander)
  {
    for( Unit unit : commander.units )
    {
      if( modelSwaps.containsKey(unit.model) )
      {
        // Store off the unit's weapons.
        unitWeapons.put(unit, unit.weapons);

        // Swap the unit's identity and store it for later.
        modelSwapBacks.put(unit, unit.model);
        unit.model = modelSwaps.get(unit.model);

        // Change out weapons.
        doWeaponSwap(unit, unit.model);

        unitsChanged.add(unit);
      }
    }
  }

  @Override
  public void revert(Commander commander)
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
