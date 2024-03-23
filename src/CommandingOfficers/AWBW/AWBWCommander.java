package CommandingOfficers.AWBW;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameScenario;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitDelta;

public abstract class AWBWCommander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage AWBW_MECHANICS_BLURB = new InfoPage(
      "COP charge gained from combat is funds damage taken and half of funds damage dealt.\n"
    + "CO powers don't charge while a power is active.\n"
    + "On each power activation, each star in your meter costs 1/5 more of the base star cost.\n"
    + "This continues for 10 activations, at which point the star cost settles at triple the base star cost.\n"
    );

  public AWBWCommander(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    // Sauce: https://docs.google.com/document/d/e/2PACX-1vQUyQIFa3sLfKU3W1ijlXQ7s_453XaJCiLle9JeBAvkbkYGcIXqVsXnlNs9xpHktTIDpEEH8xG9e9W0/pub
    // Repairing does not round up partial HP. (Note: Joining does round up partial HP)
    //     Exception: if the heal can overheal, then the chip damage will be healed for free.
    //     (8.1HP unit on city will heal to 10HP, costing 1HP of repair).
    roundUpRepairs = false;
  }

  // Charge based on funds damage taken + 1/2 dealt
  @Override
  public int calculateCombatCharge(UnitDelta minion, UnitDelta enemy, boolean isCounter)
  {
    int guiHPLoss  = minion.getHPDamage();
    int guiHPDealt =  enemy.getHPDamage();

    int power = 0; // value in funds of the charge we're getting

    power += guiHPLoss * minion.unit.getCost() / 10;
    power += guiHPDealt * enemy.unit.getCost() / 10 / 2;

    return power;
  }
  @Override
  public int calculateMassDamageCharge(Unit minion, int lostHP)
  {
    return 0;
  }

  protected abstract static class AWBWAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    UnitModifier genericAttack;
    UnitModifier genericDefense;

    protected AWBWAbility(Commander commander, String name, int cost, CostBasis basis)
    {
      super(commander, name, cost, basis);
      genericAttack  = new UnitDamageModifier(10);
      genericDefense = new UnitDamageModifier(10);
    }

    @Override
    protected final void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(genericAttack);
      modList.add(genericDefense);
      enqueueMods(gameMap, modList);
    }
    // Extra function to allow for stat mods so the above can be final.
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {}
  }
}
