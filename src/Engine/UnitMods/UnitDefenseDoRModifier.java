package Engine.UnitMods;

import Engine.Combat.StrikeParams.BattleParams;

public class UnitDefenseDoRModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public UnitDefenseDoRModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseDivision += defenseModifier;
  }
}
