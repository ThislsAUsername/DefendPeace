package Engine.UnitMods;

import Engine.Combat.StrikeParams.StrikeUnitParams;

/** Applies subtractive defense */
public class IndirectDefenseModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int defenseModifier = 0;

  public IndirectDefenseModifier(int percentChange)
  {
    defenseModifier = percentChange;
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(StrikeUnitParams params)
  {
    if( params.battleRange < 2 )
      params.defenseSubtraction += defenseModifier;
  }
}
