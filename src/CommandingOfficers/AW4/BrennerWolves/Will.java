package CommandingOfficers.AW4.BrennerWolves;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModel;

public class Will extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Will", UIUtils.SourceGames.AW4, UIUtils.BW);
      infoPages.add(new InfoPage(
          "A former Rubinelle military cadet who joined the 12th Battalion after being rescued by Captain Brenner – whom he idolises.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: Direct-attack ground units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new RallyCry(null),
          "Boosts mobility of direct-attack ground units by 2.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Will(rules);
    }
  }

  public static final int RADIUS  = 2;
  public static final int POWER   = 20;
  public static final int DEFENSE = 0;

  public Will(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = 0;
    this.boostMaskAll = UnitModel.LAND | UnitModel.DIRECT;

    addCommanderAbility(new RallyCry(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class RallyCry extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod;

    protected RallyCry(RuinedCommander commander)
    {
      super(commander, "Rally Cry");
      if( null == commander )
        return; // This isn't a "real" ability, just a scratch struct for the info page
      moveMod = new UnitTypeFilter(new UnitMovementModifier(2));
      moveMod.allOf = commander.boostMaskAll;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

}
