package Engine.UnitMods;

import Engine.Combat.StrikeParams;

public class DamageMultiplierOffense implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public DamageMultiplierOffense(int percentMultiplier)
  {
    attackModifier = percentMultiplier;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackerDamageMultiplier *= attackModifier;
    params.attackerDamageMultiplier /= 100;
  }
}
