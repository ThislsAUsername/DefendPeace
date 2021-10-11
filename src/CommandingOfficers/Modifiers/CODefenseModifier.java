package CommandingOfficers.Modifiers;

import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifierWithDefaults;

public class CODefenseModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public CODefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defensePower += defenseModifier;
  }
}
