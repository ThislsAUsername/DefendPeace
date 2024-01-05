package Engine.UnitMods;

import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Units.UnitContext;

/**
 * Filters the composed UnitModifier via subclass behavior.
 */
public abstract class UnitModFilter implements UnitModifier
{
  private static final long serialVersionUID = 1L;

  private final UnitModifier effect;
  public UnitModFilter(UnitModifier effect)
  {
    super();
    this.effect = effect;
  }

  public abstract boolean shouldApplyTo(UnitContext uc);

  @Override
  public void changeCombatContext(CombatContext instance)
  {
    if(   shouldApplyTo(instance.attacker)
       || shouldApplyTo(instance.defender) )
      effect.changeCombatContext(instance);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( shouldApplyTo(params.attacker) )
       effect.modifyUnitAttack(params);
  }

  @Override
  public void modifyUnitAttackOnUnit(BattleParams params)
  {
    if( shouldApplyTo(params.attacker) )
      effect.modifyUnitAttackOnUnit(params);
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( shouldApplyTo(params.defender) )
      effect.modifyUnitDefenseAgainstUnit(params);
  }

  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyMovePower(uc);
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyMoveType(uc);
  }

  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyAttackRange(uc);
  }

  public void modifyVision(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyVision(uc);
  }

  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyCapturePower(uc);
  }

  @Override
  public void modifyCost(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyCost(uc);
  }

  @Override
  public void modifyRepairCost(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyRepairCost(uc);
  }

  @Override
  public void modifyCargoCapacity(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyCargoCapacity(uc);
  }

  @Override
  public void modifyActionList(UnitContext uc)
  {
    if( shouldApplyTo(uc) )
      effect.modifyActionList(uc);
  }

}