package Engine.UnitMods;

import Engine.Combat.StrikeParams;

public class DirectDamageModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public DirectDamageModifier(int firepowerChange)
  {
    attackModifier = firepowerChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 )
      params.attackPower += attackModifier;
  }
}
