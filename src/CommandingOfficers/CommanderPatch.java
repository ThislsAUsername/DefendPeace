package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;

public class CommanderPatch extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Patch", new instantiator());  
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new CommanderPatch();
    }
  }

  // Variables to characterize Patch's abilities.
  private static final String PLUNDER_NAME = "Plunder";
  private static final int PLUNDER_COST = 5;
  private static final double PLUNDER_INCOME = 0.25;
  private static final int PLUNDER_ATTACK_BUFF = 10;

  private static final String PILLAGE_NAME = "Pillage";
  private static final int PILLAGE_COST = 10;
  private static final double PILLAGE_INCOME = 0.5;
  private static final int PILLAGE_ATTACK_BUFF = 25;

  private LootAbility myLootAbility = null;

  public CommanderPatch()
  {
    super(coInfo);

    addCommanderAbility(new PatchAbility(this, PLUNDER_NAME, PLUNDER_COST, PLUNDER_INCOME, PLUNDER_ATTACK_BUFF));
    addCommanderAbility(new PatchAbility(this, PILLAGE_NAME, PILLAGE_COST, PILLAGE_INCOME, PILLAGE_ATTACK_BUFF));

    // Passive - Loot
    myLootAbility = new LootAbility(this);
    GameEventListener.registerEventListener(myLootAbility);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  /** LootAbility is a passive that grants its Commander a day's income immediately upon capturing a
   *  property, so long as that property is one that generates income. */
  private static class LootAbility extends GameEventListener
  {
    private Commander myCommander = null;

    public LootAbility(Commander myCo)
    {
      myCommander = myCo;
    }

    @Override
    public void receiveCaptureEvent(Unit unit, Location location)
    {
      if( unit.CO == myCommander && location.getOwner() == myCommander && location.isProfitable() )
      {
        // We just successfully captured a property. Loot the place!
        myCommander.money += myCommander.incomePerCity;
      }
    }
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
    protected void perform(GameMap gameMap)
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
        unitCost = battleInfo.defender.model.getCost();
      }
      else if( battleInfo.defender.CO == myCommander )
      {
        hpLoss = battleInfo.attackerHPLoss;
        unitCost = battleInfo.attacker.model.getCost();
      }

      // Do the necessary math, then round to the nearest int.
      int income = (int)(hpLoss * (unitCost/10.0) * myIncomeRatio + 0.5);
      myCommander.money += income;
    }
  }
}
