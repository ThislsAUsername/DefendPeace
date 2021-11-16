package CommandingOfficers.Modifiers;

import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifierWithDefaults;

public class UnitDefenseModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public UnitDefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defensePower += defenseModifier;
  }
}
