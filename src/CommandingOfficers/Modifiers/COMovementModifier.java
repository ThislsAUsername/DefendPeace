package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import Units.UnitModel;
import Units.UnitModel.UnitEnum;
import CommandingOfficers.Commander;

public class COMovementModifier extends COModifier
{
  ArrayList<UnitModel.UnitEnum> typesToModify;
  private int rangeChange;

  public COMovementModifier(Commander user)
  {
    this(user, 1);
  }

  public COMovementModifier(Commander user, int range)
  {
    super(user);
    rangeChange = range;
    typesToModify = new ArrayList<UnitModel.UnitEnum>();
  }

  @Override
  public void apply()
  {
    for( UnitModel um : CO.unitModels )
    {
      if( typesToModify.contains(um.type) )
      {
        um.movePower = um.movePower + rangeChange;
      }
    }
  }

  public void addApplicableUnitType(UnitEnum type)
  {
    typesToModify.add(type);
  }

  @Override
  public void revert()
  {
    for( UnitModel um : CO.unitModels )
    {
      if( typesToModify.contains(um.type) )
      {
        um.movePower = um.movePower - rangeChange;
      }
    }
  }

}
