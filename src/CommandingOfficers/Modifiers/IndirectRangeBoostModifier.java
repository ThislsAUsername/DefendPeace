package CommandingOfficers.Modifiers;

import java.util.Map;

import CommandingOfficers.Commander;
import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class IndirectRangeBoostModifier extends UnitRemodelModifier implements COModifier
{
  private int boost;

  public IndirectRangeBoostModifier(int boost)
  {
    super();
    this.boost = boost;
  }

  /** Add a new unit transform assignment. */
  public Map<UnitModel, UnitModel> init(Commander commander)
  {
    modelSwaps.clear();
    for( UnitModel um : commander.unitModels )
    {
      UnitModel newModel = UnitModel.clone(um);
      boolean remodel = false;
      if( newModel.weaponModels != null )
      {
        for( WeaponModel pewpew : newModel.weaponModels )
        {
          if( pewpew.maxRange > 1 )
          {
            pewpew.maxRange += boost;
            remodel = true;
          }
        }
      }
      if( remodel )
        modelSwaps.put(um, newModel);
    }
    return modelSwaps;
  }
}
