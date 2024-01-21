package CommandingOfficers;

import Units.Unit;

public interface IValueFinder
{
  int getValue(Commander attacker, Unit unit);
}
