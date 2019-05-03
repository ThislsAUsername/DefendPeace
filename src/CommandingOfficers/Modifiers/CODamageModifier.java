package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Units.UnitModel;

public class CODamageModifier implements COModifier
{
  ArrayList<UnitModel> modelsToModify;
  private int attackModifier = 0;

  /**
   * Modify a Commander's units' firepower by the specified amount.
   * By default it will affect all UnitModels owned by the Commander.
   * To impact only specific units, use addApplicableUnitModel to set
   * the ones that should be modified.
   * @param firepowerChange
   */
  public CODamageModifier(int firepowerChange)
  {
    attackModifier = firepowerChange;
    modelsToModify = new ArrayList<UnitModel>();
  }

  /**
   * Add a UnitModel type for this modifier to affect. If no models are added
   * this way, then by default apply will affect all of the Commander's UnitModels.
   * @param model
   */
  public void addApplicableUnitModel(UnitModel model)
  {
    if( model != null )
    {
      modelsToModify.add(model);
    }
    else
    {
      System.out.println("Attempting to add null model to COMovementModifier!");
      throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
    }
  }

  @Override
  public void apply(Commander commander)
  {
    if( modelsToModify.isEmpty() )
    {
      modelsToModify.addAll(commander.unitModels);
    }

    for( UnitModel um : modelsToModify )
    {
      if( um.weaponModels != null )
      {
        um.modifyDamageRatio(attackModifier);
      }
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( UnitModel um : modelsToModify )
    {
      if( um.weaponModels != null )
      {
        um.modifyDamageRatio(-attackModifier);
      }
    }
  }
}
