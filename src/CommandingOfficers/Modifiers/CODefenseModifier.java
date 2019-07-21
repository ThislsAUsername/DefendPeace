package CommandingOfficers.Modifiers;

import Units.UnitModel;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericCOModifier;

public class CODefenseModifier extends GenericCOModifier
{
  private int defenseModifier = 0;

  public CODefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void applyChanges(Commander commander, ArrayList<UnitModel> models)
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
  public void revertChanges(Commander commander, ArrayList<UnitModel> models)
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
