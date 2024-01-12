package Engine.UnitMods;

import Engine.Combat.StrikeParams;

public class IndirectDamageModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public IndirectDamageModifier(int firepowerChange)
  {
    attackModifier = firepowerChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange > 1 )
      params.attackPower += attackModifier;
  }
}
