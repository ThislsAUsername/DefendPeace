package CommandingOfficers.BrennerWolves;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.RuinedCommander;
import Engine.GameScenario;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.*;

public class BrennerDoR extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Brenner", UIUtils.SourceGames.AW4, UIUtils.BW);
      infoPages.add(new InfoPage(
          "Captain of the 12th Battalion. He has an unshakable faith in humanity's goodness, and will aid any in need.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: 3\n" +
          "Zone Boost: All units +20 DD\n"));
      infoPages.add(new InfoPage(
          "Reinforce ("+RuinedAbility.COST+"):\n" +
          "Heal +3 HP to your units, even those in transports.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new BrennerDoR(rules);
    }
  }
  public static final int RADIUS  = 3;
  public static final int POWER   = 0;
  public static final int DEFENSE = 20;

  public BrennerDoR(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);

    addCommanderAbility(new Reinforce(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class Reinforce extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;

    protected Reinforce(RuinedCommander commander)
    {
      super(commander, "Reinforce");
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit u : myCommander.army.getUnits() )
      {
        u.alterHealth(30);
      }
      super.perform(gameMap);
    }
  }

}
