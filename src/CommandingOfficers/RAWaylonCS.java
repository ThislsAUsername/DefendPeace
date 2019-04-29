package CommandingOfficers;

import CommandingOfficers.Modifiers.UnitTypeDefenseModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class RAWaylonCS extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Waylon", new instantiator());
  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "--WAYLON--\r\n" + 
          "Air units gain +20% firepower and +25% defense.\r\n"
          + "xxXXXX\n" + 
          "WINGMAN: All air units gain +25% defense.\r\n" + 
          "BAD COMPANY: All air units gain +50% defense."));
    }
    @Override
    public Commander create()
    {
      return new RAWaylonCS();
    }
  }

  public RAWaylonCS()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
    {
      if( um.chassis == ChassisEnum.AIR_HIGH || um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(20);
        um.modifyDefenseRatio(25);
      }
    }

    addCommanderAbility(new AirDefBonus(this, "Wingman", 2, 25));
    addCommanderAbility(new AirDefBonus(this, "Bad Company", 6, 50));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class AirDefBonus extends CommanderAbility
  {
    private int power = 1;

    AirDefBonus(Commander commander, String name, int cost, int buff)
    {
      super(commander, name, cost);
      power = buff;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      UnitTypeDefenseModifier airDefMod = new UnitTypeDefenseModifier(power);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.chassis == ChassisEnum.AIR_HIGH ||  um.chassis == ChassisEnum.AIR_LOW)
        {
          airDefMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(airDefMod);
    }
  }
}