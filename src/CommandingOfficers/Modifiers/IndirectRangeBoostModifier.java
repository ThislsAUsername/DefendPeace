package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.Map;

import CommandingOfficers.Commander;
import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class IndirectRangeBoostModifier implements COModifier
{
  ArrayList<WeaponModel> modelsToModify;
  private int boost;

  public IndirectRangeBoostModifier(Commander commander, int boost)
  {
    this.boost = boost;
    modelsToModify = new ArrayList<WeaponModel>();
    for( UnitModel um : commander.unitModels )
    {
      if( um.weaponModels != null )
      {
        for( WeaponModel pewpew : um.weaponModels )
        {
          if( pewpew.maxRange > 1 )
          {
            modelsToModify.add(pewpew);
          }
        }
      }
    }
  }

  @Override
  public void apply(Commander commander)
  {
    for( WeaponModel pewpew : modelsToModify )
    {
      if( pewpew.maxRange > 1 )
      {
        pewpew.maxRange += boost;
      }
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( WeaponModel pewpew : modelsToModify )
    {
      if( pewpew.maxRange > 1 )
      {
        pewpew.maxRange -= boost;
      }
    }
  }
}
