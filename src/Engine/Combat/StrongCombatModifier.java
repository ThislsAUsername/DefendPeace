package Engine.Combat;

import CommandingOfficers.Commander;

// Permanent boost of X%, which is multiplied by a factor
// The factor is reset to 1 at the beginning of every turn
public class StrongCombatModifier extends CombatModifier
{
  private int boost;
  private double factor;
  
  public StrongCombatModifier(Commander user, int ATKboost)
  {
    super(user);
    boost = ATKboost;
    factor = 1;
  }

  public void alterCombat(CombatParameters params)
  {
    if (params.attacker.CO == CO)
    {
      params.attackFactor += boost*factor;
    }
  }
  
  public void multiplyBoost(double multiplier)
  {
    factor = multiplier;
  }
  
  public void turn(Commander activeCO)
  {
    if( activeCO == CO )
    {
      factor = 1;
    }
  }
}
