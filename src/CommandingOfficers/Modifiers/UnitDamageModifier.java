package CommandingOfficers.Modifiers;

import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifierWithDefaults;

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
