package CommandingOfficers.Modifiers;

import Units.UnitModel;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;

public class CODefenseModifier extends GenericUnitModifier
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public CODefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  protected final void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      if( um.weaponModels != null )
      {
        um.modifyDefenseRatio(defenseModifier);
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
        um.modifyDefenseRatio(-defenseModifier);
      }
    }
  }
}
