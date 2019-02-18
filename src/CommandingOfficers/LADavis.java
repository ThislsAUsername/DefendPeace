package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class LADavis extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Davis AW1Eagle", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new LADavis();
    }
  }

  public LADavis()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.AIR_HIGH || um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(15);
        um.modifyDefenseRatio(10);
        um.idleFuelBurn -= 2;
      }
      if( um.chassis == ChassisEnum.SHIP || um.chassis == ChassisEnum.SUBMERGED )
      {
        um.modifyDamageRatio(-20);
      }
    }

    addCommanderAbility(new CowardFlight(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class CowardFlight extends CommanderAbility
  {
    private static final String NAME = "Coward's Flight";
    private static final int COST = 6;

    CowardFlight(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        if( unit.model.chassis != ChassisEnum.TROOP ) // don't penalize units who haven't moved yet 
        {
          unit.isTurnOver = false;
        }
      }
      myCommander.addCOModifier(new CODamageModifier(-30));
      myCommander.addCOModifier(new CODefenseModifier(-40));
    }
  }
}
