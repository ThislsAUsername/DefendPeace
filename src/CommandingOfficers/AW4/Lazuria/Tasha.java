package CommandingOfficers.AW4.Lazuria;

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

public class Tasha extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Tasha", UIUtils.SourceGames.AW4, UIUtils.LA);
      infoPages.add(new InfoPage(
          "A Lazurian soldier who burns with the desire to avenge her late brother.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: Air units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new SonicBoom(null),
          "Boosts mobility of air units by 2.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Tasha(rules);
    }
  }

  public static final int RADIUS  = 1;
  public static final int POWER   = 40;
  public static final int DEFENSE = 20;

  public Tasha(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = UnitModel.AIR_HIGH | UnitModel.AIR_LOW;

    addCommanderAbility(new SonicBoom(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class SonicBoom extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod;

    protected SonicBoom(RuinedCommander commander)
    {
      super(commander, "Sonic Boom");
      if( null == commander )
        return; // This isn't a "real" ability, just a scratch struct for the info page
      moveMod = new UnitTypeFilter(new UnitMovementModifier(2));
      moveMod.oneOf = commander.boostMaskAny;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

}
