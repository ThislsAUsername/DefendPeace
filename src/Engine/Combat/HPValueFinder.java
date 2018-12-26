package Engine.Combat;

import CommandingOfficers.Commander;
import Units.Unit;

public class HPValueFinder implements IValueFinder
{
  protected Commander co;
  protected boolean avoidAllies;
  
  public HPValueFinder(Commander cmdr, boolean avoidAllies)
  {
    co = cmdr;
    this.avoidAllies = avoidAllies;
  }
  
  @Override
  public int getValue(Unit unit)
  {
    int HPValue = unit.getHP();
    if (co.isEnemy(unit.CO))
    {
      return HPValue;
    }
    else if (avoidAllies)
    {
      return -HPValue;
    }
    return 0;
  }
}
