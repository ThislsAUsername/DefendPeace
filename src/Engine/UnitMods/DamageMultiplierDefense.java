package Engine.UnitMods;

import Engine.Combat.StrikeParams.StrikeUnitParams;

public class DamageMultiplierDefense implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public DamageMultiplierDefense(int percentMultiplier)
  {
    attackModifier = percentMultiplier;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(StrikeUnitParams params)
  {
    params.defenderDamageMultiplier *= attackModifier;
    params.defenderDamageMultiplier /= 100;
  }
}
