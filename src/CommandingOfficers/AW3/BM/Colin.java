package CommandingOfficers.AW3.BM;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.ModifyFundsEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.UnitContext;

public class Colin extends AW3Commander
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
      super("Colin", UIUtils.SourceGames.AW3, UIUtils.BM);
      infoPages.add(new InfoPage(
            "Colin (AW3)\n"
          + "Blue Moon's rich boy CO and Sasha’s little brother. A gifted CO with a sharp, if insecure mind.\n"
          + "Heir to a vast fortune, Colin can buy units at bargain-basement prices. His troops’ low firepower stems from his lack of experience.\n"
          + "(80% prices, -10 attack)"));
      infoPages.add(new InfoPage(new GoldRush(null, null),
            "Increases deployment funds by one and a half times.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new PowerOfMoney(null, null),
            "Uses wealth to increase the strength of weapons. The more funds available, the stronger the weapons become.\n"
          + "(+Y attack, where Y is funds on activation/300)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Olaf and Grit\n"
          + "Miss: Black Hole"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
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

  private static class GoldRush extends AW3Ability
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
    return funds / 300;
  }
  private static class PowerOfMoney extends AW3Ability
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
