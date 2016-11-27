package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import CommandingOfficers.Commander;

public class COMovementModifier implements COModifier
{
  ArrayList<UnitModel.UnitEnum> typesToModify;
  private int rangeChange;

  public COMovementModifier()
  {
    this(1);
  }

  public COMovementModifier(int range)
  {
    rangeChange = range;
    typesToModify = new ArrayList<UnitModel.UnitEnum>();
  }

  @Override
  public void apply(Commander commander)
  {
    for( UnitModel um : commander.unitModels )
    {
      if( typesToModify.contains(um.type) )
      {
        um.movePower = um.movePower + 1;
      }
    }
  }

  public void addApplicableUnitType(UnitEnum type)
  {
    typesToModify.add(type);
  }

  @Override
  public void revert(Commander commander)
  {
    // TODO Auto-generated method stub

  }

}
