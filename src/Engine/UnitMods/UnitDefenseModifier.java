package Engine.UnitMods;

import Engine.Combat.StrikeParams.StrikeUnitParams;

/** Applies subtractive defense */
public class UnitDefenseModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public UnitDefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(StrikeUnitParams params)
  {
    params.defenseSubtraction += defenseModifier;
  }
}
