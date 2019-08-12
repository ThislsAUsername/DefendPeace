package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameScenario;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Terrain.Location;
import Terrain.MapMaster;
import Units.Unit;

public class Patch extends Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Patch");
      infoPages.add(new InfoPage(
          "Commander Patch is a pirate, who does piratey things like lootin' and plunderin'\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" + 
          "- Patch gets a turn's worth of income from any property he captures\n"));
      infoPages.add(new InfoPage(
          "Plunder ("+PLUNDER_COST+"):\n" + 
          "Gives an attack boost of "+PLUNDER_ATTACK_BUFF+"%\n" + 
          "Gives "+PLUNDER_INCOME+"x of the value of any funds damage Patch deals.\n"));
      infoPages.add(new InfoPage(
          "Pillage ("+PILLAGE_COST+"):\n" + 
          "Gives an attack boost of "+PILLAGE_ATTACK_BUFF+"%\n" + 
          "Gives "+PILLAGE_INCOME+"x the value of any funds damage Patch deals.\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Patch(rules);
    }
  }

  // Variables to characterize Patch's abilities.
  private static final String PLUNDER_NAME = "Plunder";
  private static final int PLUNDER_COST = 3;
  private static final double PLUNDER_INCOME = 0.25;
  private static final int PLUNDER_ATTACK_BUFF = 10;

  private static final String PILLAGE_NAME = "Pillage";
  private static final int PILLAGE_COST = 6;
  private static final double PILLAGE_INCOME = 0.5;
  private static final int PILLAGE_ATTACK_BUFF = 25;

  private LootAbility myLootAbility = null;

  public Patch(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

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
    private static final long serialVersionUID = 1L;
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
        myCommander.money += myCommander.gameRules.incomePerCity;
      }
    }
  }

  /** PatchAbility gives Patch a damage bonus, and also grants
   * income based on damage inflicted to enemies. */
  private static class PatchAbility extends CommanderAbility implements COModifier
  {
    private static final long serialVersionUID = 1L;
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
    protected void perform(MapMaster gameMap)
    {
      // Register this class as a COModifier, so we can deactivate one turn from now.
      myCommander.addCOModifier(this);

      // Register the damage-to-income listener.
      GameEventListener.registerEventListener(listener);

      // Bump up our power level.
      myCommander.addCOModifier(damageBuff);
    }

    @Override // COModifier interface.
    public void applyChanges(Commander commander)
    {
      // No special action required.
    }

    @Override
    public void revertChanges(Commander commander)
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
    private static final long serialVersionUID = 1L;
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
