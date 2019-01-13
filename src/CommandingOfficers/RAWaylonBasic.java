package CommandingOfficers;

import CommandingOfficers.Modifiers.UnitTypeDefenseModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class RAWaylonBasic extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Waylon\nBasic", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new RAWaylonBasic();
    }
  }

  public RAWaylonBasic()
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

    addCommanderAbility(new AirDefBonus(this, "Wingman", 5, 50));
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
