package CommandingOfficers.DefendPeace.BM;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.StateTrackers.DamageDealtToIncomeConverter;
import UI.UIUtils;
import Units.Unit;
import Units.UnitContext;
import Terrain.MapLocation;
import Terrain.MapMaster;

public class Sasha extends AWBWCommander
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
      super("Sasha", UIUtils.SourceGames.VARIANTS, UIUtils.BM, "CAP");
      infoPages.add(new InfoPage(
            "Sasha (CAP)\n"
          + "Receives +100 funds per property that grants funds and she owns. (Note: labs, comtowers, and 0 Funds games do not get additional income).\n"
          + "0.9x capture speed, but get half the property's income every time you take the capture action.\n"));
      infoPages.add(new InfoPage(new MarketCrash(null, null),
            "Reduces enemy power bar(s) by (10 * Funds / 5000)% of their maximum power bar.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new WarBonds(null, null),
            "Receives funds equal to 50% of the damage dealt when attacking enemy units.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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
    if( 0 != gameRules.incomePerCity )
      incomeAdjustment = 100;

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new MarketCrash(this, cb));
    addCommanderAbility(new WarBonds(this, cb));
  }

  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    uc.capturePower -= 10;
  }
  @Override
  public GameEventQueue receiveCaptureEvent(Unit unit, Commander prevOwner, MapLocation location)
  {
    GameEventQueue returnEvents = new GameEventQueue();
    if( unit.CO == this && location.isProfitable() )
    {
      int onePropFunds = gameRules.incomePerCity + incomeAdjustment;
      returnEvents.add(new ModifyFundsEvent(army, onePropFunds/2));
    }
    return returnEvents;
  }

  private static class MarketCrash extends AWBWAbility
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
      int drainPercent = myCommander.army.money * 10 / 5000;
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

  private static class WarBonds extends AWBWAbility
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
