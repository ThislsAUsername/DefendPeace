package CommandingOfficers.Modifiers;

import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitModifier;

public class CODefenseModifier implements UnitModifier
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
