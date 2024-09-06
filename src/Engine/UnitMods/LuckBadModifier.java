package Engine.UnitMods;

import Engine.Combat.StrikeParams;

public class LuckBadModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int luckModifier = 0;

  public LuckBadModifier(int luckChange)
  {
    luckModifier = luckChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.luckRolledBad += luckModifier;
  }
}
