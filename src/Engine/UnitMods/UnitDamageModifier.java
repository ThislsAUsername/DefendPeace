package Engine.UnitMods;

import Engine.Combat.StrikeParams;

public class UnitDamageModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public UnitDamageModifier(int firepowerChange)
  {
    attackModifier = firepowerChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += attackModifier;
  }
}
