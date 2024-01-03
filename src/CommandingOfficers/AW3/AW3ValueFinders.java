package CommandingOfficers.AW3;

import CommandingOfficers.Commander;
import CommandingOfficers.IValueFinder;
import Engine.UnitActionFactory;
import Units.Unit;
import Units.UnitModel;

public abstract class AW3ValueFinders
{
  public static class CostValueFinder implements IValueFinder
  {
    int maxDamage = 3;
    @Override
    public int getValue(Commander attacker, Unit unit)
    {
      // Ignore Oozium, if we ever add those
      int health = unit.getHealth();
      if( health < 10 )
        return 2;
      health = Math.min(health, maxDamage * 10);

      int value = unit.getCost() * health / UnitModel.MAXIMUM_HEALTH;

      if( !unit.CO.isEnemy(attacker) )
        return -value;
      return value;
    }
  }
  public static class HealthValueFinder implements IValueFinder
  {
    int maxDamage = 3;
    public int getValue(Commander attacker, Unit unit)
    {
      // Ignore Oozium, if we ever add those
      int health = unit.getHealth();
      if( health < 10 )
        return 1;
      health = Math.min(health, maxDamage * 10);

      if( !unit.CO.isEnemy(attacker) )
        return -health;
      return health;
    }
  }
  public static class CaptureValueFinder implements IValueFinder
  {
    int maxDamage = 3;
    public int getValue(Commander attacker, Unit unit)
    {
      // Ignore Oozium, if we ever add those
      int health = unit.getHealth();
      if( health < 10 )
        return 1;
      health = Math.min(health, maxDamage * 10);

      for( UnitActionFactory action : unit.model.baseActions ) // Does it make sense for this to be dynamic?
      {
        if( action == UnitActionFactory.CAPTURE )
        {
          health *= 4;
          if( unit.getCaptureProgress() > 0 )
            health *= 2;
          break;
        }
      }

      if( !unit.CO.isEnemy(attacker) )
        return -health;
      return health;
    }
  }
}
