package Engine.UnitMods;

import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;

/**
 * UnitModifiers exist to represent transient or conditional changes in a unit's properties.
 * <p>They are expected to have some external framework in place to manage their lifetimes.
 * <p>This deprecates COModifiers.
 */
public interface UnitModifier
{
  /**
   * Do not call this directly; call {@link UnitModList.apply()} instead.
   * <p>Exists as a hook to let the modifier know what list it's been added to.
   */
  void applyToUMLImpl(UnitModList uml);

  /**
   * Allows a UnitModifier to make drastic combat changes like counterattacking first or at 2+ range.
   * <p>Prefer using the other combat hooks when feasible.
   */
  default void changeCombatContext(CombatContext instance)
  {
  }

  /**
   * Called any time a unit makes a weapon attack;
   *   applies to all potential targets, whether they be units or not.
   * <p>Should be used to modify attacks from units
   *   any time you do not need specific information about the target.
   */
  default void modifyUnitAttack(StrikeParams params)
  {
  }

  /**
   * Called any time you are attacking a unit, always after {@link #modifyUnitAttack(StrikeParams)}
   * <p>Applies only when attacking a unit.
   * <p>Should be used only when you need specific information about your target.
   */
  default void modifyUnitAttackOnUnit(BattleParams params)
  {
  }

  /**
   * Called any time your unit is being attacked, after {@link #modifyUnitAttackOnUnit(BattleParams)}
   * <p>Should be used to modify attacks made against your units.
   */
  default void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
  }

}