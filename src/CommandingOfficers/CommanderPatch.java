package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameInstance;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;

public class CommanderPatch extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Patch", CommanderLibrary.CommanderEnum.PATCH);

  // Variables to characterize Patch's abilities.
  private static final String PLUNDER_NAME = "Plunder";
  private static final int PLUNDER_COST = 0;
  private static final double PLUNDER_INCOME = 0.25;
  private static final int PLUNDER_ATTACK_BUFF = 10;

  private static final String PILLAGE_NAME = "Pillage";
  private static final int PILLAGE_COST = 0;
  private static final double PILLAGE_INCOME = 0.5;
  private static final int PILLAGE_ATTACK_BUFF = 25;

  public CommanderPatch()
  {
    super(coInfo);

    addCommanderAbility(new PatchAbility(this, PLUNDER_NAME, PLUNDER_COST, PLUNDER_INCOME, PLUNDER_ATTACK_BUFF));
    addCommanderAbility(new PatchAbility(this, PILLAGE_NAME, PILLAGE_COST, PILLAGE_INCOME, PILLAGE_ATTACK_BUFF));

    // Passive - Loot
    // TODO: Patch has a capture bonus of 1 day's income when he first takes a property.
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  /** PatchAbility gives Patch a damage bonus, and also grants
   * income based on damage inflicted to enemies. */
  private static class PatchAbility extends CommanderAbility implements COModifier
  {
    private DamageDealtToIncomeConverter listener = null;
    private CODamageModifier damageBuff = null;

    PatchAbility(Commander myCO, String abilityName, int abilityCost, double incomeRatio, int unitBuff)
    {
      super(myCO, abilityName, abilityCost);

      // Create an object to handle receiving battle outcomes and generating income.
      listener = new DamageDealtToIncomeConverter(myCO, incomeRatio);

      // Create a COModifier that we can apply to Patch when needed.
      damageBuff = new CODamageModifier(unitBuff);
    }

    @Override
    protected void perform(GameInstance game)
    {
      // Register this class as a COModifier, so we can deactivate one turn from now.
      myCommander.addCOModifier(this);

      // Register the damage-to-income listener.
      GameEventListener.registerEventListener(listener);

      // Bump up our power level.
      myCommander.addCOModifier(damageBuff);
    }

    @Override // COModifier interface.
    public void apply(Commander commander)
    {
      // No special action required.
    }

    @Override
    public void revert(Commander commander)
    {
      // This will be called when the Commander removes this COModifier. It will remove the damage
      // modifier we added as well, so we just need to turn off the the damage-to-income listener.
      GameEventListener.unregisterEventListener(listener);
    }
  }

  /**
   * This GameEventListener will inspect published BattleEvents. If the owning Commander took
   * part in the battle, then any damage done to the opponent will give income to the owning
   * Commander. The amount of income is a fraction of value of the damage that was done, thus
   * damaging more expensive units will grant more income.
   */
  private static class DamageDealtToIncomeConverter extends GameEventListener
  {
    private Commander myCommander = null;
    private double myIncomeRatio = 0.0;

    public DamageDealtToIncomeConverter(Commander myCo, double incomeRatio)
    {
      myCommander = myCo;
      myIncomeRatio = incomeRatio;
    }

    @Override
    public void receiveBattleEvent(BattleSummary battleInfo)
    {
      // Determine if we were part of this fight. If so, cash in on any damage done to the other guy.
      double hpLoss = 0;
      double unitCost = 0;
      if( battleInfo.attacker.CO == myCommander )
      {
        hpLoss = battleInfo.defenderHPLoss;
        unitCost = battleInfo.defender.model.moneyCost;
      }
      else if( battleInfo.defender.CO == myCommander )
      {
        hpLoss = battleInfo.attackerHPLoss;
        unitCost = battleInfo.attacker.model.moneyCost;
      }

      // Do the necessary math, then round to the nearest int.
      int income = (int)(hpLoss * (unitCost/10.0) * myIncomeRatio + 0.5);
      myCommander.money += income;
    }
  }
}
