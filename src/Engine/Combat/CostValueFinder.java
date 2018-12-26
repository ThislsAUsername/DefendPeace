package Engine.Combat;

import CommandingOfficers.Commander;
import Units.Unit;

public class CostValueFinder implements IValueFinder
{
  protected Commander co;
  protected boolean avoidAllies;
  
  public CostValueFinder(Commander cmdr, boolean avoidAllies)
  {
    co = cmdr;
    this.avoidAllies = avoidAllies;
  }
  
  @Override
  public int getValue(Unit unit)
  {
    int fundsValue = unit.model.getCost()*unit.getHP();
    if (co.isEnemy(unit.CO))
    {
      return fundsValue;
    }
    else if (avoidAllies)
    {
      return -fundsValue;
    }
    return 0;
  }
}
