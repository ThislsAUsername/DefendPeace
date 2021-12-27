package Engine.UnitMods;

import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Units.UnitContext;

/**
 * Defaults all the interface items to no-ops for ease of implementation
 */
public interface UnitModifierWithDefaults extends UnitModifier
{
  @Override
  default void changeCombatContext(CombatContext instance)
  {
  }

  @Override
  default void modifyUnitAttack(StrikeParams params)
  {
  }

  @Override
  default void modifyUnitAttackOnUnit(BattleParams params)
  {
  }

  @Override
  default void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
  }

  @Override
  default void modifyCost(UnitContext uc)
  {
  }

  @Override
  default void modifyRepairCost(UnitContext uc)
  {
  }

  @Override
  default void modifyMovePower(UnitContext uc)
  {
  }

  @Override
  default void modifyAttackRange(UnitContext uc)
  {
  }

  @Override
  default void modifyActionList(UnitContext uc)
  {
  }
}