package Engine.Combat;

import CommandingOfficers.Commander;

public class CombatDamageModifier extends CombatModifier
{
  private int boost;
  
  public CombatDamageModifier(Commander user, int ATKboost)
  {
    super(user);
    boost = ATKboost;
  }

  public void alterCombat(CombatParameters params)
  {
    if (params.attacker.CO == CO)
    {
      params.attackFactor += boost;
    }
  }
}
