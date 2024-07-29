package CommandingOfficers.AWBW.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Colin extends AWBWCommander
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
      super("Colin", UIUtils.SourceGames.AWBW, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Colin (AWBW)\n"
          + "Unit cost is reduced to 80% (20% cheaper), but lose -10% attack.\n"));
      infoPages.add(new InfoPage(new GoldRush(null, null),
            "Funds are multiplied by 1.5x.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new PowerOfMoney(null, null),
            "Unit attack percentage increases by (3 * Funds / 1000)%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Colin(rules);
    }
  }

  public Colin(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new GoldRush(this, cb));
    addCommanderAbility(new PowerOfMoney(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower -= 10;
  }
  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costRatio -= 20;
  }

  private static class GoldRush extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Gold Rush";
    private static final int COST = 2;

    GoldRush(Colin commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
      AIFlags |= PHASE_BUY;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEventQueue events = new GameEventQueue();
      int bonusFunds = myCommander.army.money / 2;
      events.add(new ModifyFundsEvent(myCommander.army, bonusFunds));
      return events;
    }
  }

  public static int calcSuperBoost(int funds)
  {
    return 3 * funds / 1000;
  }
  private static class PowerOfMoney extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Power of Money";
    private static final int COST = 6;

    PowerOfMoney(Colin commander, CostBasis cb)
    {
      super(commander, NAME, COST, cb);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      int boost = calcSuperBoost(myCommander.army.money);
      modList.add(new UnitDamageModifier(boost));
    }
  }

}
