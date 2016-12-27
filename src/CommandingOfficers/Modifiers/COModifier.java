package CommandingOfficers.Modifiers;

import CommandingOfficers.Commander;
import Engine.Combat.CombatParameters;

/** COModifier provides static bonuses to units,
 and also provides an interface for manipulating battles directly. */
public abstract class COModifier
{
  public boolean done;
  protected Commander CO;

  public COModifier(Commander user)
  {
    done = false;
    CO = user;
  }

  public void alterCombat(CombatParameters params)
  {}

  public void turn()
  {
    done = true;
  }

  public void apply()
  {}
  public void revert()
  {}
}
