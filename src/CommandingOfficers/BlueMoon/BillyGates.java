package CommandingOfficers.BlueMoon;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class BillyGates extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Billy Gates");
      infoPages.add(new InfoPage(
          "Billy Gates (rebalanced Colin)\r\n" + 
          "  Units cost -20% less to build, but lose -10/-10 stats\r\n" + 
          "Gold Rush (4): Funds are multiplied by 1.2\r\n" + 
          "Power of Money (6): Unit attack percentage increases by (3 * Funds/1000)%"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new BillyGates(rules);
    }
  }

  public BillyGates(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      um.modifyDamageRatio(-10);
      um.modifyDefenseRatio(-10);
      um.COcost = 0.8;
    }

    addCommanderAbility(new GoldRush(this));
    addCommanderAbility(new PowerOfMoney(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class GoldRush extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Gold Rush";
    private static final int COST = 4;
    private static final double VALUE = 1.2;
    BillyGates COcast;

    GoldRush(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BillyGates) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.money *= VALUE;
    }
  }

  private static class PowerOfMoney extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Power of Money";
    private static final int COST = 6;
    private static final double VALUE = 3.333/1000;
    BillyGates COcast;

    PowerOfMoney(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BillyGates) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(new CODamageModifier((int) (COcast.money*VALUE)));
    }
  }
}

