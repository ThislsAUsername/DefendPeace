package CommandingOfficers;

import Engine.GameScenario;
import Engine.UnitMods.UnitDamageModifier;
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
    + "COP charge gained from combat is funds damage taken and half of funds damage dealt.\n"
    + "On each power activation, each star in your meter costs 1/5 more of the base star cost.\n"
    + "This continues for 9 activations, at which point the star cost settles at double the base star cost.\n"
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
    // TODO: Copy/paste super's code and delete the HP-based bit once the parent PR settles down.
    return super.calculateCombatCharge(minion, enemy, isCounter);
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

    TrilogyAbility(int genericAtt, int genericDef, Commander commander, String name, int cost, CostBasis basis)
    {
      super(commander, name, cost, basis);
      genericAttack  = new UnitDamageModifier(genericAtt);
      genericDefense = new UnitDamageModifier(genericDef);
      basis.maxStarRatio = basis.baseStarRatio * 2; // 18k per star
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(genericAttack);
      modList.add(genericDefense);
    }
  }
}
