package Engine.UnitMods;

import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;

/**
 * Boosts attack and defense in equal measure
 */
public class UnitFightStatModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int modifier = 0;

  public UnitFightStatModifier(int percentChange)
  {
    modifier = percentChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += modifier;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defensePower += modifier;
  }
}
