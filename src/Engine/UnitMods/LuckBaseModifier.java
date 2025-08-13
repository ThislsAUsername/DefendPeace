package Engine.UnitMods;

import Engine.Combat.StrikeParams;

public class LuckBaseModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int luckModifier = 0;

  public LuckBaseModifier(int luckChange)
  {
    luckModifier = luckChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
      params.luckBase += luckModifier;
  }
}
