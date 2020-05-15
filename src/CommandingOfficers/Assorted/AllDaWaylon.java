package CommandingOfficers.Assorted;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class AllDaWaylon extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("AllDaWaylon");
      infoPages.add(new InfoPage(
          "--All da Way...lon--\r\n" +
          "Air units gain +20% firepower and +30% defense.\r\n" +
          "xxxXXX\n" +
          "WINGMAN: All air units gain +20% defense.\r\n" +
          "BAD COMPANY: All air units gain +45% defense."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new AllDaWaylon(rules);
    }
  }

  public AllDaWaylon(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isAirUnit() )
      {
        um.modifyDamageRatio(20);
        um.modifyDefenseRatio(30);
      }
    }

    addCommanderAbility(new AirDefBonus(this, "Wingman", 3, 20));
    addCommanderAbility(new AirDefBonus(this, "Bad Company", 6, 45));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class AirDefBonus extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
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
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isAirUnit() )
        {
          airDefMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(airDefMod);
    }
  }
}

