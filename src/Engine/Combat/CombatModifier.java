package Engine.Combat;

import CommandingOfficers.Commander;

public class CombatModifier
{
  public boolean done;
  public Commander CO;
  
  public CombatModifier(Commander user)
  {
    done = false;
    CO = user;
  }

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
