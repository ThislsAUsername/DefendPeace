package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Units.UnitModel;

public class UnitTypeDamageModifier implements COModifier
{
  ArrayList<UnitModel> modelsToModify;
  private int damageBuff;

  public UnitTypeDamageModifier(int buff)
  {
    damageBuff = buff;
    modelsToModify = new ArrayList<UnitModel>();
  }

  @Override
  public void apply(Commander commander)
  {
    for( UnitModel um : modelsToModify)
    {
      um.modifyDamageRatio(damageBuff);
    }
  }

  public void addApplicableUnitModel(UnitModel model)
  {
    if( model != null )
    {
      modelsToModify.add(model);
    }
    else
    {
      System.out.println("Attempting to add null model to UnitTypeDamageModifier!");
      throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( UnitModel um : modelsToModify )
    {
      um.modifyDamageRatio(-damageBuff);
    }
  }

}
