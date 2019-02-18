package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class BWWillCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Will", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BWWillCS();
    }
  }

  public BWWillCS()
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
            um.modifyDamageRatio(20);
        }
      }
    }

    addCommanderAbility(new RallyCry(this));
    addCommanderAbility(new ANewEra(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class RallyCry extends CommanderAbility
  {
    private static final String NAME = "Rally Cry";
    private static final int COST = 2;

    RallyCry(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(1);
      for( UnitModel um : myCommander.unitModels )
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }

  private static class ANewEra extends CommanderAbility
  {
    private static final String NAME = "A New Era";
    private static final int COST = 5;

    ANewEra(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(2);
      for( UnitModel um : myCommander.unitModels )
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }
}
