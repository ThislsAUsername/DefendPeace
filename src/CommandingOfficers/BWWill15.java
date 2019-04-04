package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class BWWill15 extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Will 15", new instantiator());
  private static class instantiator extends COMaker
  {
    @Override
    public Commander create()
    {
      return new BWWill15();
    }
  }

  public BWWill15()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
      {
        if( um.weaponModels != null )
        {
          boolean buff = false;
          for( WeaponModel pewpew : um.weaponModels )
          {
            if( pewpew.canFireAfterMoving )
            {
              buff = true;
            }
          }
          if( buff )
            um.modifyDamageRatio(15);
        }
      }
    }

    addCommanderAbility(new GoFast(this, "Rally Cry", 2, 1));
    addCommanderAbility(new GoFast(this, "A New Era", 5, 2));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class GoFast extends CommanderAbility
  {
    private final int power;

    GoFast(Commander commander, String name, int cost, int oomph)
    {
      super(commander, name, cost);
      power = oomph;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(power);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
        {
          if( um.weaponModels != null )
          {
            boolean buff = false;
            for( WeaponModel pewpew : um.weaponModels )
            {
              if( pewpew.canFireAfterMoving )
              {
                buff = true;
              }
            }
            if( buff )
              moveMod.addApplicableUnitModel(um);
          }
        }
      }
      myCommander.addCOModifier(moveMod);
    }
  }
}
