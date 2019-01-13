package CommandingOfficers;

import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class BWLinCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Lin\nCS", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BWLinCS();
    }
  }

  public BWLinCS()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP)
      {
        um.modifyDamageRatio(10);
      }
    }

    addCommanderAbility(new Scout(this));
    addCommanderAbility(new NightVision(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Scout extends CommanderAbility
  {
    private static final String NAME = "Scout";
    private static final int COST = 2;

    Scout(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // add vision +1 and piercing vision to land units
    }
  }

  private static class NightVision extends CommanderAbility
  {
    private static final String NAME = "Night Vision";
    private static final int COST = 5;

    NightVision(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // add vision +2 and piercing vision to land units
    }
  }
}
