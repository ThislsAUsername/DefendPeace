package CommandingOfficers.AWBW;

import Engine.GameScenario;

import java.util.ArrayList;

import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AWBW.AWBWCommander.AWBWAbility;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitDelta;

/**
 * An AWBW-style CO that also has the COU mechanic, but no zone.
 */
public abstract class COUableCommander extends DeployableCommander
{
  private static final long serialVersionUID = 1L;

  public final int couPowBase; // static values that define the power the COU should stay at
  public final int couDefBase;
  private int couPow; // floating values that dip on powers to match the above
  private int couDef;

  @Override
  public int getCOUCount() {return 1;}

  public COUableCommander(int atk, int def, CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    couPowBase = atk;
    couDefBase = def;
    couPow = couPowBase;
    couDef = couDefBase;
    // Sauce: https://docs.google.com/document/d/e/2PACX-1vQUyQIFa3sLfKU3W1ijlXQ7s_453XaJCiLle9JeBAvkbkYGcIXqVsXnlNs9xpHktTIDpEEH8xG9e9W0/pub
    // Repairing does not round up partial HP. (Note: Joining does round up partial HP)
    //     Exception: if the heal can overheal, then the chip damage will be healed for free.
    //     (8.1HP unit on city will heal to 10HP, costing 1HP of repair).
    roundUpRepairs = false;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( COUs.contains(params.attacker.unit) )
      params.attackPower += couPow;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( COUs.contains(params.defender.unit) )
      params.defenseSubtraction += couDef;
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


  /**
   * An AWBW-standard power that simulates applying (part of) the COU/boost stats to your whole army.<p>
   * Called "non-stacking" because this boost doesn't stack with the D2D "COU" boost.
   */
  protected static class NonStackingBoost extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    COUableCommander COcast;
    private int atk, def;
    UnitTypeFilter powMod;
    UnitTypeFilter defMod;

    protected NonStackingBoost(COUableCommander commander, String name, int cost, CostBasis basis, int pAtk, int pDef)
    {
      super(commander, name, cost, basis);
      COcast = commander;
      atk = pAtk;
      def = pDef;
      powMod = new UnitTypeFilter(new UnitDamageModifier(atk));
      defMod = new UnitTypeFilter(new UnitDefenseModifier(def));
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.couPow -= atk;
      COcast.couDef -= def;
      powMod.allOf = COcast.canDeployMask;
      defMod.allOf = COcast.canDeployMask;
    }

    @Override
    protected void revert(MapMaster gameMap)
    {
      COcast.couPow = COcast.couPowBase;
      COcast.couDef = COcast.couDefBase;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(powMod);
      modList.add(defMod);
    }
  }

}
