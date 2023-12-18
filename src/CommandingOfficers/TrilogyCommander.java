package CommandingOfficers;

import Engine.GameScenario;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitDelta;
import java.util.ArrayList;
import CommandingOfficers.CommanderInfo.InfoPage;

/** Base class for AW2/3 COs (AW1 is special) */
public abstract class TrilogyCommander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage TRILOGY_MECHANICS_BLURB = new InfoPage(
      "CO powers never charge while a power is active.\n"
    + "On each power activation, each star in your meter costs 1/5 more of the base star cost.\n"
    + "This continues for 9 activations, after which the star cost settles at double the base star cost.\n"
    + "CO power activation deducts the *new* power cost rather than the old one.\n"
    );

  public TrilogyCommander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
  }

  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    if( null != getActiveAbility() )
      return 0;
    if( minion == null || enemy == null )
      return 0;

    int guiHPLoss  = minion.getHealthDamage() / 10;
    int guiHPDealt =  enemy.getHealthDamage() / 10;

    int power = 0; // value in funds of the charge we're getting

    // Add up the funds value of the damage done to both participants.
    power += guiHPLoss * minion.unit.getCost() / 10;
    // The damage we deal is worth half as much as the damage we take, to help powers be a comeback mechanic.
    power += guiHPDealt * enemy.unit.getCost() / 10 / 2;

    return power;
  }
  @Override
  public int calculateMassDamageCharge(Unit minion, int lostHP)
  {
    return 0;
  }

  protected abstract static class TrilogyAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    UnitModifier genericAttack;
    UnitModifier genericDefense;

    protected TrilogyAbility(int genericAtt, int genericDef, Commander commander, String name, int cost, CostBasis basis)
    {
      super(commander, name, cost, basis);
      genericAttack  = new UnitDamageModifier(genericAtt);
      genericDefense = new UnitDefenseModifier(genericDef);
      basis.maxStarRatio = basis.baseStarRatio * 2; // 18k per star
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(genericAttack);
      modList.add(genericDefense);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Change my CO's energy such that it's as if this power cost the activation cost of the next power rather than the actual cost
      int costAtActivation = baseStars * costBasis.calcCostPerStar(costBasis.numCasts - 1);
      int costForNextCast  = costBasis.calcCost(baseStars);
      int extraEnergyCost  = costForNextCast - costAtActivation; // Will be negative on cast 9
      myCommander.modifyAbilityPower(-1 * extraEnergyCost);
    }
  }
}
