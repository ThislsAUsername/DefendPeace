package CommandingOfficers.Modifiers;

import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;

public class CODamageModifier implements UnitModifier
{
  private static final long serialVersionUID = 1L;
  private int attackModifier = 0;

  public CODamageModifier(int firepowerChange)
  {
    attackModifier = firepowerChange;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += attackModifier;
  }
}
