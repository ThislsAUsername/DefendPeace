package CommandingOfficers.Lazuria;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class Davis extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Davis");
      infoPages.add(new InfoPage(
          "Davis (AW1 Eagle)\n"
        + "  Air units gain +15/10 stats and consume -2 fuel per day. Naval units lose -20% attack\r\n"));
      infoPages.add(new InfoPage(
          "Coward's Flight (6):\n"
        + "All non-footsoldier units lose -30/-40 stats. (This comes out to 80/70 stats for tanks)\n"
        + "All non-footsoldier units may move and fire again even if built this turn (use this power after moving!)"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Davis(rules);
    }
  }

  public Davis(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isAirUnit() )
      {
        um.modifyDamageRatio(15);
        um.modifyDefenseRatio(10);
        um.idleFuelBurn -= 2;
      }
      if( um.isSeaUnit() )
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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Coward's Flight";
    private static final int COST = 6;

    CowardFlight(Commander commander)
    {
      super(commander, NAME, COST);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        if( unit.model.isNone(UnitModel.TROOP) )
        {
          unit.isTurnOver = false;
        }
      }
      myCommander.addCOModifier(new CODamageModifier(-30));
      myCommander.addCOModifier(new CODefenseModifier(-40));
    }
  }
}

