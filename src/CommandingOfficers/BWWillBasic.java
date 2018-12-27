package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.GameMap;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class BWWillBasic extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Will\nBasic", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BWWillBasic();
    }
  }

  public BWWillBasic()
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
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class RallyCry extends CommanderAbility
  {
    private static final String NAME = "Rally Cry";
    private static final int COST = 4;

    RallyCry(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
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
