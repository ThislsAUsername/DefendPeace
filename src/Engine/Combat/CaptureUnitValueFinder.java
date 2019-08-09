package Engine.Combat;

import CommandingOfficers.Commander;
import Engine.UnitActionType;
import Units.Unit;

public class CaptureUnitValueFinder implements IValueFinder
{
  protected Commander co;
  protected boolean avoidAllies;

  public CaptureUnitValueFinder(Commander cmdr, boolean avoidAllies)
  {
    co = cmdr;
    this.avoidAllies = avoidAllies;
  }

  @Override
  public int getValue(Unit unit)
  {
    int captureValue = 0;
    for( UnitActionType action : unit.model.possibleActions )
    {
      if( action == UnitActionType.CAPTURE )
      {
        captureValue++;
        break;
      }
    }
    if( unit.getCaptureProgress() > 0 && unit.getCaptureProgress() < 20 )
      captureValue++;

    if( co.isEnemy(unit.CO) )
    {
      return captureValue;
    }
    else if( avoidAllies )
    {
      return -captureValue;
    }
    return 0;
  }
}
