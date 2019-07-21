package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericCOModifier;
import Units.UnitModel;

public class COMovementModifier extends GenericCOModifier
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
  public void applyChanges(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.movePower = um.movePower + rangeChange;
    }
  }

  @Override
  public void revertChanges(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.movePower = um.movePower - rangeChange;
    }
  }

}
