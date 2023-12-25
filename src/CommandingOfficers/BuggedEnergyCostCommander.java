package CommandingOfficers;

import Engine.GameScenario;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import java.util.ArrayList;
import CommandingOfficers.CommanderInfo.InfoPage;

/** Base class for AW2/3 COs (AW1 is special) */
public abstract class BuggedEnergyCostCommander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage TRILOGY_MECHANICS_BLURB = new InfoPage(
      "CO powers don't charge while a power is active.\n"
    + "On each power activation, each star in your meter costs 1/5 more of the base star cost.\n"
    + "This continues for 9 activations, after which the star cost settles at double the base star cost.\n"
    + "Using a power costs the energy required for the next activation of that power, rather than the advertized cost.\n"
    );

  public BuggedEnergyCostCommander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
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
      // AW2 and 3 have a bug(?) where the energy cost is modified before it is deducted.
      // To implement that behavior, change my CO's energy such that it's as if this power cost
      //   the next activation's cost rather than the advertized cost.
      int costAtActivation = baseStars * costBasis.calcCostPerStar(costBasis.numCasts - 1);
      int costForNextCast  = costBasis.calcCost(baseStars);
      int extraEnergyCost  = costForNextCast - costAtActivation; // Will be negative on cast 9
      myCommander.modifyAbilityPower(-1 * extraEnergyCost);
    }
  }
}
