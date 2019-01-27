package CommandingOfficers.Modifiers;

import Units.UnitModel;
import CommandingOfficers.Commander;

public class COVisionModifier implements COModifier
{
  private int visionBoost = 0;

  public COVisionModifier(int boost)
  {
    visionBoost = boost;
  }

  @Override
  public void apply(Commander commander)
  {
    for( UnitModel um : commander.unitModels )
    {
      um.visionRange += visionBoost;
      um.visionIgnoresCover = true;
    }
  }

  @Override
  public void revert(Commander commander)
  {
    for( UnitModel um : commander.unitModels )
    {
      um.visionRange -= visionBoost;
      um.visionIgnoresCover = false;
    }
  }
}
