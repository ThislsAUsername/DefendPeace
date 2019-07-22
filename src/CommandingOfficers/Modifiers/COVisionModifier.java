package CommandingOfficers.Modifiers;

import Units.UnitModel;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;

public class COVisionModifier extends GenericUnitModifier
{
  private int visionBoost = 0;

  public COVisionModifier(int boost)
  {
    visionBoost = boost;
  }

  @Override
  protected final void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models)
    {
      um.visionRange += visionBoost;
      um.visionRangePiercing = um.visionRange;
    }
  }

  @Override
  protected final void restoreUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models)
    {
      um.visionRange -= visionBoost;
      um.visionRangePiercing = 1; // TODO: Fix this
    }
  }
}
