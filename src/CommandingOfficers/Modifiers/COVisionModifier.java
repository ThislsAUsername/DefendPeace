package CommandingOfficers.Modifiers;

import Units.UnitModel;

import java.util.ArrayList;

import CommandingOfficers.Commander;

public class COVisionModifier implements COModifier
{
  ArrayList<UnitModel> modelsToModify;
  private int visionBoost = 0;

  public COVisionModifier(int boost)
  {
    visionBoost = boost;
    modelsToModify = new ArrayList<UnitModel>();
  }

  @Override
  public void apply(Commander commander)
  {
    for( UnitModel um : modelsToModify)
    {
      um.visionRange += visionBoost;
      um.visionRangePiercing = um.visionRange;
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
      System.out.println("Attempting to add null model to COVisionModifier!");
      throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( UnitModel um : modelsToModify)
    {
      um.visionRange -= visionBoost;
      um.visionRangePiercing = 1; // TODO: Fix this
    }
  }
}
