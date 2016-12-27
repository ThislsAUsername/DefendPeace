package Engine.Combat;

import CommandingOfficers.Commander;

public class CombatModifier
{
  public boolean done = false;
  public Commander CO = null;

  public void alterCombat(CombatParameters params)
  {}
  public void turn(Commander activeCO)
  {
    if( activeCO == CO )
    {
      done = true;
    }
  }

}
