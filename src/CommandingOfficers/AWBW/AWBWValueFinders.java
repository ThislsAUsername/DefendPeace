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
  /**
   * The VALUE of a unit is the same as under the HP missile, but multiplied by the cost of the unit.<p>
   * With the exception of units with less than 1.0 HP, whose VALUE is 2G.<p>
   * Not implemented: The tiebreaker is the highest total (HP x Cost of the unit) of the enemies hit. If it's still a tie, the most top and left tile is picked.
   */
  public static class CostValueFinder implements IValueFinder
  {
    public int maxDamage = 3;
    public boolean selfHarm = true;
    @Override
    public int getValue(Commander attacker, Unit unit)
    {
      int hp = unit.getHealth();
      if( hp < 10 )
      {
        if( !unit.CO.isEnemy(attacker) )
          return -2;
        return 2;
      }
      hp = Math.min(hp, maxDamage*10);

      int value = unit.getCost() * hp / 100;

      if( !unit.CO.isEnemy(attacker) )
      {
        if( selfHarm )
          return -value;
        return 0;
      }
      return value;
    }
  }
  /**
   * The VALUE is the raw HP of the unit hit, with no modifier for cost. It is positive for enemy units and negative for friendly units.<p>
   * However, any units with more than 3.0 HP are treated as having 3.0 HP and any units with less than 1.0 HP are treated as having 0.1 HP.<p>
   * Not implemented: The tiebreaker is the highest total HP of the enemy units hit. If it is still a tie, the most top and left tile is picked.
   */
  public static class HealthValueFinder implements IValueFinder
  {
    public int maxDamage = 3;
    public int getValue(Commander attacker, Unit unit)
    {
      int value = unit.getHealth();
      if( value < 10 )
      {
        if( !unit.CO.isEnemy(attacker) )
          return -1;
        return 1;
      }
      value = Math.min(value, maxDamage*10);

      if( !unit.CO.isEnemy(attacker) )
        return -value;
      return value;
    }
  }
  /**
   * The VALUE of a unit is the same as under the HP missile with the following changes:<p>
   * the VALUE of an Infantry or a Mech with more than 1.0 HP is multiplied by 4 and is multiplied again by 2 if it is capturing a property (for a total multiplier of 8).<p>
   * Unlike the HP missile, the only tiebreaker is the "top and left tiebreaker".
   */
  public static class CaptureValueFinder implements IValueFinder
  {
    public int maxDamage = 3;
    public int getValue(Commander attacker, Unit unit)
    {
      int value = unit.getHealth();
      if( value < 10 )
      {
        if( !unit.CO.isEnemy(attacker) )
          return -1;
        return 1;
      }
      value = Math.min(value, maxDamage*10);

      for( UnitActionFactory action : unit.model.baseActions ) // Does it make sense for this to be dynamic?
      {
        if( action == UnitActionFactory.CAPTURE )
        {
          value *= 4;
          if( unit.getCaptureProgress() > 0 )
            value *= 2;
          break;
        }
      }

      if( !unit.CO.isEnemy(attacker) )
        return -value;
      return value;
    }
  }
}
