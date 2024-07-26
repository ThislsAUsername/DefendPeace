package Engine.UnitMods;

import Engine.Combat.StrikeParams;

/**
 * Multiplies attack when countering
 */
public class CounterMultiplierModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int modifier;

  public CounterMultiplierModifier(int percentChange)
  {
    modifier = percentChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.isCounter )
    {
      params.attackerDamageMultiplier *= modifier;
      params.attackerDamageMultiplier /= 100;
    }
  }
}
