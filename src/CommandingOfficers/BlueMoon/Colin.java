package CommandingOfficers.BlueMoon;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class Colin extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Colin");
      infoPages.add(new InfoPage(
          "Colin\r\n" + 
          "  Units cost -20% less to build, but lose -10% attack\r\n" + 
          "Gold Rush -- Funds are multiplied by 1.5\r\n" + 
          "Power of Money -- Unit attack percentage increases by (3 * Funds/1000)%"));
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

    for( UnitModel um : unitModels.values() )
    {
      um.modifyDamageRatio(-10);
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
    private static final String NAME = "Gold Rush";
    private static final int COST = 2;
    private static final double VALUE = 1.5;
    Colin COcast;

    GoldRush(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Colin) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.money *= VALUE;
    }
  }

  private static class PowerOfMoney extends CommanderAbility
  {
    private static final String NAME = "Power of Money";
    private static final int COST = 6;
    private static final double VALUE = 3.333/1000;
    Colin COcast;

    PowerOfMoney(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Colin) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(new CODamageModifier((int) (COcast.money*VALUE)));
    }
  }
}

