package CommandingOfficers.Modifiers;

import Units.UnitModel;
import CommandingOfficers.Commander;

public class CODefenseModifier implements COModifier
{
  private int defenseModifier = 0;

  public CODefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void apply(Commander commander)
  {
    for( UnitModel um : commander.unitModels )
    {
      if( um.weaponModels != null )
      {
        um.modifyDefenseRatio(defenseModifier);
      }
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( UnitModel um : commander.unitModels )
    {
      if( um.weaponModels != null )
      {
        um.modifyDefenseRatio(-defenseModifier);
      }
    }
  }
}
