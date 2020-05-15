package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class Adder extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Adder");
      infoPages.add(new InfoPage(
          "Adder\n" +
          "D2D: Thinks he's T1\n" +
          "  Sideslip -- +1 Movement to all units\n" +
          "Sidewinder -- +2 Movement to all units"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Adder(rules);
    }
  }

  public Adder(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new Sideslip(this));
    addCommanderAbility(new Sidewinder(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Sideslip extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Sideslip";
    private static final int COST = 2;

    Sideslip(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(1);
      for(UnitModel um : myCommander.unitModels)
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }

  private static class Sidewinder extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Sidewinder";
    private static final int COST = 5;

    Sidewinder(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(2);
      for(UnitModel um : myCommander.unitModels)
      {
        moveMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(moveMod);
    }
  }
}

