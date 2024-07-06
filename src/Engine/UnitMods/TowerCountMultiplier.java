package Engine.UnitMods;

import Engine.Combat.CombatContext;
import Units.UnitContext;

public class TowerCountMultiplier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private final int towerMult;

  public TowerCountMultiplier(int towerMult)
  {
    this.towerMult = towerMult;
  }

  @Override
  public void changeCombatContext(CombatContext instance, UnitContext minion)
  {
    minion.towerCountDS  *= towerMult;
    minion.towerCountDoR *= towerMult;
  }
}
