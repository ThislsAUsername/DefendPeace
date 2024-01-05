package CommandingOfficers.AW4.BrennerWolves;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import Engine.UnitMods.VisionModifier;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModel;

public class Lin extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Lin", UIUtils.SourceGames.AW4, UIUtils.BW);
      infoPages.add(new InfoPage(
          "The 12th Battalion's unflappable second in command. She has tremendous respect for Captain Brenner, although they disagree often.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: Direct-attack ground units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new Scout(null),
          "Adds 2 to ground-unit vision, including hiding places.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Lin(rules);
    }
  }

  public static final int RADIUS  = 1;
  public static final int POWER   = 20;
  public static final int DEFENSE = 20;

  public Lin(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = UnitModel.LAND;

    addCommanderAbility(new Scout(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class Scout extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter sightMod;

    protected Scout(RuinedCommander commander)
    {
      super(commander, "Scout");
      if( null == commander )
        return; // This isn't a "real" ability, just a scratch struct for the info page
      sightMod = new UnitTypeFilter(new VisionModifier(2));
      sightMod.allOf = commander.boostMaskAll;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
    }
  }

}
