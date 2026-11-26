package Engine.UnitMods;

import Engine.Combat.StrikeParams.StrikeUnitParams;

public class UnitDefenseDoRModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public UnitDefenseDoRModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(StrikeUnitParams params)
  {
    params.defenseDivision += defenseModifier;
  }
}
