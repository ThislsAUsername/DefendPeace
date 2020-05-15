package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Units.UnitModel;
import Units.WeaponModel;

public class IndirectRangeBoostModifier extends GenericUnitModifier
{
  private static final long serialVersionUID = 1L;
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
      for( WeaponModel pewpew : um.weapons )
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
      for( WeaponModel pewpew : um.weapons )
      {
        if( pewpew.maxRange > 1 )
        {
          pewpew.maxRange -= boost;
        }
      }
    }
  }
}
