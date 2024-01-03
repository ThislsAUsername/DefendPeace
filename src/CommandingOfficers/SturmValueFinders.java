package CommandingOfficers;

import Units.Unit;
import Units.UnitModel;

public abstract class SturmValueFinders // Until such time as we find out AW1 works substantially differently
{
  public static class HPValueFinder implements IValueFinder
  {
    public boolean countHidden = true; // Ignore cloaked units even if we can see them
    public int getValue(Commander attacker, Unit unit)
    {
      if( !countHidden && unit.model.hidden )
        return 0;
      int hp = unit.getHP();
      if( hp <= 1 )
        return 0;

      if( !unit.CO.isEnemy(attacker) )
        return -hp;
      return hp;
    }
  }
  public static class CostValueFinder implements IValueFinder
  {
    public boolean countHidden = true; // Ignore cloaked units even if we can see them
    public int indirectMultiplier = 1;
    @Override
    public int getValue(Commander attacker, Unit unit)
    {
      if( !countHidden && unit.model.hidden )
        return 0;
      int hp = unit.getHP();
      if( hp <= 1 )
        return 0;

      int value = unit.getCost() * hp / 10;
      if( unit.model.isAny(UnitModel.INDIRECT) )
        value *= indirectMultiplier;

      if( !unit.CO.isEnemy(attacker) )
        return -value;
      return value;
    }
  }
}
