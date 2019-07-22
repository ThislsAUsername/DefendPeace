package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Units.UnitModel;

public class COMovementModifier extends GenericUnitModifier
{
  private int rangeChange;

  public COMovementModifier()
  {
    this(1);
  }

  public COMovementModifier(int range)
  {
    rangeChange = range;
  }

  @Override
  public void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.movePower = um.movePower + rangeChange;
    }
  }

  @Override
  public void restoreUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.movePower = um.movePower - rangeChange;
    }
  }

}
