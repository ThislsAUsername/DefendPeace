package CommandingOfficers.Assorted;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class WaylonCS extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Waylon");
      infoPages.add(new InfoPage(
          "--WAYLON--\r\n" +
          "Air units gain +20% firepower and +25% defense.\r\n" +
          "xxxXXX\n" +
          "WINGMAN: All air units gain +25% defense.\r\n" +
          "BAD COMPANY: All air units gain +50% defense."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new WaylonCS(rules);
    }
  }

  public WaylonCS(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels.values() )
    {
      if( um.chassis == ChassisEnum.AIR_HIGH || um.chassis == ChassisEnum.AIR_LOW )
      {
        um.modifyDamageRatio(20);
        um.modifyDefenseRatio(25);
      }
    }

    addCommanderAbility(new AirDefBonus(this, "Wingman", 3, 25));
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
      CODefenseModifier airDefMod = new CODefenseModifier(power);
      for( UnitModel um : myCommander.unitModels.values() )
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

