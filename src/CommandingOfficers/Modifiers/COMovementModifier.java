package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Units.UnitModel;

public class COMovementModifier implements COModifier
{
  ArrayList<UnitModel> modelsToModify;
  private int rangeChange;

  public COMovementModifier()
  {
    this(1);
  }

  public COMovementModifier(int range)
  {
    rangeChange = range;
    modelsToModify = new ArrayList<UnitModel>();
  }

  @Override
  public void apply(Commander commander)
  {
    for( UnitModel um : modelsToModify)
    {
      um.movePower = um.movePower + rangeChange;
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
      System.out.println("Attempting to add null model to COMovementModifier!");
      throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( UnitModel um : modelsToModify )
    {
      um.movePower = um.movePower - rangeChange;
    }
  }

}
