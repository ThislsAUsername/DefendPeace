package CommandingOfficers.Modifiers;

import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifierWithDefaults;

/**
 * Boosts attack and defense in equal measure
 */
public class COFightStatModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int modifier = 0;

  public COFightStatModifier(int percentChange)
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
