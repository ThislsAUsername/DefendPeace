package CommandingOfficers;

import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class LAGageCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Gage", new instantiator());

  private static class instantiator extends COMaker
  {
    @Override
    public Commander create()
    {
      return new LAGageCS();
    }
  }

  public LAGageCS()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      boolean buff = false;
      if( um.chassis == ChassisEnum.SHIP )
        buff = true;
      if( !buff && um.weaponModels != null )
      {
        for( WeaponModel pewpew : um.weaponModels )
        {
          if( !pewpew.canFireAfterMoving )
          {
            buff = true;
          }
        }
      }
      if( buff )
      {
        um.modifyDamageRatio(20);
        um.modifyDefenseRatio(10);
      }
    }

    addCommanderAbility(new LongShot(this));
    addCommanderAbility(new LongBarrel(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class LongShot extends CommanderAbility
  {
    private static final String NAME = "Long Shot";
    private static final int COST = 2;
    private static final int VALUE = 1;
    LAGageCS COcast;

    LongShot(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (LAGageCS) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(VALUE);
      rangeBoost.init(COcast);
      COcast.addCOModifier(rangeBoost);
    }
  }

  private static class LongBarrel extends CommanderAbility
  {
    private static final String NAME = "Long Barrel";
    private static final int COST = 5;
    private static final int VALUE = 2;
    LAGageCS COcast;

    LongBarrel(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (LAGageCS) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(VALUE);
      rangeBoost.init(COcast);
      COcast.addCOModifier(rangeBoost);
    }
  }
}
