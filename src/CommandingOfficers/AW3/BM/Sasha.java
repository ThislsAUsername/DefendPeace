package CommandingOfficers.AW3.BM;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.StateTrackers.DamageDealtToIncomeConverter;
import UI.UIUtils;
import Terrain.MapMaster;

public class Sasha extends AW3Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Sasha", UIUtils.SourceGames.AW3, UIUtils.BM, "");
      infoPages.add(new InfoPage(
            "Sasha (AW3)\n"
          + "Colin's sister. Normally ladylike, but becomes daring when angry.\n"
          + "Being the heir to a vast fortune, she gets an additional 100 funds from allied bases.\n"));
      infoPages.add(new InfoPage(new MarketCrash(null, null),
            "Decreases the enemy's CO Power meters in proportion to the funds in the treasury.\n"
          + "(100% of max for 50k funds)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new WarBonds(null, null),
            "Earn funds when she inflicts damage on a foe. She gains 50% of the unit's cost in damage.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Truffles\n"
          + "Miss: Pork rinds"));
      infoPages.add(AW3_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sasha(rules);
    }
  }

  public Sasha(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    incomeAdjustment = 100;

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new MarketCrash(this, cb));
    addCommanderAbility(new WarBonds(this, cb));
  }

  private static class MarketCrash extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Market Crash";
    private static final int COST = 2;

    MarketCrash(Sasha commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags |= PHASE_BUY;
    }
    @Override
    protected void perform(MapMaster map)
    {
      int drainPercent = myCommander.army.money * 100 / 50_000;
      for( Army a : map.game.armies )
        if( myCommander.isEnemy(a) )
          for( Commander co : a.cos )
          {
            int maxEnergy = co.getMaxAbilityPower();
            int drain = drainPercent * maxEnergy / 100;
            co.modifyAbilityPower(-drain);
          }
    }
  }

  private static class WarBonds extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "War Bonds";
    private static final int COST = 6;
    private DamageDealtToIncomeConverter tracker;

    WarBonds(Sasha commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
    }
    @Override
    public void initForGame(GameInstance game)
    {
      tracker = DamageDealtToIncomeConverter.instance(game, DamageDealtToIncomeConverter.class);
    }
    @Override
    protected void perform(MapMaster gameMap)
    {
      tracker.startTracking(myCommander.army, 50);
    }
    @Override
    public void revert(MapMaster gameMap)
    {
      tracker.stopTracking(myCommander.army, 50);
    }
  }

}
