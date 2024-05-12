package Engine.UnitMods;

import Engine.Army;
import Engine.Combat.CombatContext;
import Units.UnitContext;

public class TowerCountMultiplier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private final int towerMult;
  private final Army toBoost;

  public TowerCountMultiplier(int towerMult, Army toBoost)
  {
    this.towerMult = towerMult;
    this.toBoost = toBoost;
  }

  @Override
  public void changeCombatContext(CombatContext instance)
  {
    doTheThing(instance.attacker);
    doTheThing(instance.defender);
  }
  private void doTheThing(UnitContext minion)
  {
    if( toBoost == minion.CO.army )
    {
      minion.towerCountDS  *= towerMult;
      minion.towerCountDoR *= towerMult;
    }
  }
}
