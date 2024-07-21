package CommandingOfficers.AW2.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.UnitMods.DamageMultiplierOffense;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;
import Units.UnitModel;

public class Colin extends AW2Commander
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
      super("Colin", UIUtils.SourceGames.AW2, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Colin (AW2)\n"
          + "Blue Moon's little rich boy CO. Views Olaf and Grit with awe and admiration.\n"
          + "The heir to a vast fortune. Purchases all units at a special low price. Troops' low firepower stems from his lack of experience.\n"
          + "(80% prices, -10 attack)"));
      infoPages.add(new InfoPage(new GoldRush(null, null),
            "Increases deployment funds by one and a half times.\n"
          + "+10 defense\n"));
      infoPages.add(new InfoPage(new PowerOfMoney(null, null),
            "Uses wealth to increase the strength of weapons. The more funds available, the stronger the weapons become.\n"
          + "(damage x 1+Y/100, where Y is funds on activation/300)\n"
          + "+10 defense\n"));
      infoPages.add(new InfoPage(
            "Hit: Olaf and Grit\n"
          + "Miss: Black Hole"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
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

  private static class GoldRush extends AW2Ability
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

  public static int calcSuperMult(int funds)
  {
    int boost = funds / 300;
    boost += UnitModel.DEFAULT_STAT_RATIO;
    return boost;
  }
  private static class PowerOfMoney extends AW2Ability
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
      int boost = calcSuperMult(myCommander.army.money);
      modList.add(new DamageMultiplierOffense(boost));
    }
  }

}
