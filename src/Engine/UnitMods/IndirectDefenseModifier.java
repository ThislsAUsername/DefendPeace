package Engine.UnitMods;

import Engine.Combat.StrikeParams.BattleParams;

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
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.battleRange < 2 )
      params.defenseSubtraction += defenseModifier;
  }
}
