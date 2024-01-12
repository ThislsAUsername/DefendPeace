package Engine.UnitMods;

import Engine.Combat.StrikeParams.BattleParams;

public class DamageMultiplierDefense implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public DamageMultiplierDefense(int percentMultiplier)
  {
    attackModifier = percentMultiplier;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenderDamageMultiplier *= attackModifier;
    params.defenderDamageMultiplier /= 100;
  }
}
