package CommandingOfficers.AW4.NRA;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitDefenseDoRModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModel;

public class Waylon extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Waylon", UIUtils.SourceGames.AW4, UIUtils.NW);
      infoPages.add(new InfoPage(
          "A member of the Rubinelle Army who seeks only to fulfill his own pleasures and desires.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: Air units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new Wingman(null),
          "Boost air unit defense by 270 (410 + veterancy, total).\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Waylon(rules);
    }
  }

  public static final int RADIUS  = 2;
  public static final int POWER   = 20;
  public static final int DEFENSE = 30;

  public Waylon(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = UnitModel.AIR_HIGH | UnitModel.AIR_LOW;

    addCommanderAbility(new Wingman(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class Wingman extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter defMod;

    protected Wingman(RuinedCommander commander)
    {
      super(commander, "Wingman");
      if( null == commander )
        return; // This isn't a "real" ability, just a scratch struct for the info page
      defMod = new UnitTypeFilter(new UnitDefenseDoRModifier(270));
      defMod.oneOf = commander.boostMaskAny;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(defMod);
    }
  }

}
