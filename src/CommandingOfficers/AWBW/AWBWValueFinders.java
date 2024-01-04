package CommandingOfficers.AWBW;

import CommandingOfficers.Commander;
import CommandingOfficers.IValueFinder;
import Engine.UnitActionFactory;
import Units.Unit;

/**
 * Writeup is here: https://awbw.fandom.com/wiki/Rachel<p>
 * Sturm and VB have cost missiles, and Sturm's has to center on a unit<p>
 * All ignore fog.
 */
public abstract class AWBWValueFinders
{
  public static class CostValueFinder implements IValueFinder
  {
    public int maxDamage = 3;
    public boolean selfHarm = true;
    @Override
    public int getValue(Commander attacker, Unit unit)
    {
      int hp = unit.getHP();
      hp = Math.min(hp, maxDamage);

      int value = unit.getCost() * hp / 10;

      if( !unit.CO.isEnemy(attacker) )
      {
        if( selfHarm )
          return -value;
        return 0;
      }
      return value;
    }
  }
  public static class HealthValueFinder implements IValueFinder
  {
    public int maxDamage = 3;
    public int getValue(Commander attacker, Unit unit)
    {
      int value = unit.getHP();
      value = Math.min(value, maxDamage);

      if( !unit.CO.isEnemy(attacker) )
        return -value;
      return value;
    }
  }
  public static class CaptureValueFinder implements IValueFinder
  {
    public int getValue(Commander attacker, Unit unit)
    {
      if( !unit.CO.isEnemy(attacker) )
        return 0; // Note that friendlies are not subtracted, just ignored.

      int value = 0;

      for( UnitActionFactory action : unit.model.baseActions ) // Does it make sense for this to be dynamic?
      {
        if( action == UnitActionFactory.CAPTURE )
        {
          ++value;
          if( unit.getCaptureProgress() > 0 )
            ++value;
          break;
        }
      }

      return value;
    }
  }
}
