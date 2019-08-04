package CommandingOfficers.Modifiers;

import Engine.GameScenario;
import java.util.ArrayList;
import java.util.Map;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class IndirectRangeBoostModifier extends GenericUnitModifier
{
  private int boost;

  public IndirectRangeBoostModifier(int boost)
  {
    this.boost = boost;
  }

  @Override
  protected final void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      for( WeaponModel pewpew : um.weaponModels )
      {
        if( pewpew.maxRange > 1 )
        {
          pewpew.maxRange += boost;
        }
      }
    }
  }

  @Override
  protected final void restoreUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      for( WeaponModel pewpew : um.weaponModels )
      {
        if( pewpew.maxRange > 1 )
        {
          pewpew.maxRange -= boost;
        }
      }
    }
  }
}
