package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Units.UnitModel;

public class CODamageModifier extends GenericUnitModifier
{
  private static final long serialVersionUID = 1L;
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
  }

  @Override
  protected final void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      if( um.weaponModels != null )
      {
        um.modifyDamageRatio(attackModifier);
      }
    }
  }

  @Override
  protected final void restoreUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      if( um.weaponModels != null )
      {
        um.modifyDamageRatio(-attackModifier);
      }
    }
  }
}
