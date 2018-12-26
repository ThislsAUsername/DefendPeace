package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import Terrain.GameMap;
import Units.UnitModel;

public class Colin extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Colin", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new Colin();
    }
  }

  public Colin()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
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
    protected void perform(GameMap gameMap)
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
    protected void perform(GameMap gameMap)
    {
      COcast.addCOModifier(new CODamageModifier((int) (COcast.money*VALUE)));
    }
  }
}
