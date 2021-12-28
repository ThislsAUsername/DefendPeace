package Engine.UnitMods;

import java.io.Serializable;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Units.UnitContext;

/**
 * UnitModifiers exist to represent transient or conditional changes in a unit's properties.
 * <p>They are expected to have some external framework in place to manage their lifetimes.
 */
public interface UnitModifier extends Serializable
{
  /**
   * Allows a UnitModifier to make drastic combat changes like counterattacking first or at 2+ range.
   * <p>Prefer using the other combat hooks when feasible.
   */
  void changeCombatContext(CombatContext instance);

  /**
   * Called any time a unit makes a weapon attack;
   *   applies to all potential targets, whether they be units or not.
   * <p>Should be used to modify attacks from units
   *   any time you do not need specific information about the target.
   */
  void modifyUnitAttack(StrikeParams params);

  /**
   * Called any time you are attacking a unit, always after {@link #modifyUnitAttack(StrikeParams)}
   * <p>Applies only when attacking a unit.
   * <p>Should be used only when you need specific information about your target.
   */
  void modifyUnitAttackOnUnit(BattleParams params);

  /**
   * Called any time your unit is being attacked, after {@link #modifyUnitAttackOnUnit(BattleParams)}
   * <p>Should be used to modify attacks made against your units.
   */
  void modifyUnitDefenseAgainstUnit(BattleParams params);

  /**
   * Should always be called for cost-related calculations
   * <p>Parameter fields may be null, so beware
   */
  void modifyCost(UnitContext uc);
  void modifyRepairCost(UnitContext uc);

  void modifyMovePower(UnitContext uc);
  void modifyMoveType(UnitContext uc);
  void modifyAttackRange(UnitContext uc);

  void modifyCargoCapacity(UnitContext uc);
  void modifyActionList(UnitContext uc);

}