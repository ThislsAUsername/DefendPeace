package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class LATasha1star extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Tasha");
      infoPages.add(new InfoPage(
          "--TASHA--\r\n" + 
          "Air units gain +40% firepower and +15% defense.\r\n" + 
          "xXXXX\r\n" + 
          "SONIC BOOM: All air units gain +1 movement.\r\n" + 
          "FOX ONE: All air units gain +2 movement."));
    }
    @Override
    public Commander create()
    {
      return new LATasha1star();
    }
  }

  public LATasha1star()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.AIR_HIGH || um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(40);
        um.modifyDefenseRatio(15);
      }
    }

    addCommanderAbility(new AirMoveBonus(this, "Sonic Boom", 1, 1));
    addCommanderAbility(new AirMoveBonus(this, "Fox One", 4, 2));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class AirMoveBonus extends CommanderAbility
  {
    private int power = 1;

    AirMoveBonus(Commander commander, String name, int cost, int buff)
    {
      super(commander, name, cost);
      power = buff;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier airMoveMod = new COMovementModifier(power);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.chassis == ChassisEnum.AIR_HIGH ||  um.chassis == ChassisEnum.AIR_LOW)
        {
          airMoveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(airMoveMod);
    }
  }
}

