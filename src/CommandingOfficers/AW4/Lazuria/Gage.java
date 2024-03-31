package CommandingOfficers.AW4.Lazuria;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModel;

public class Gage extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Gage", UIUtils.SourceGames.AW4, UIUtils.LA);
      infoPages.add(new InfoPage(
          "A Lazurian soldier. He is a man of few words, but a consummate professional.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: Sea and indirect units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new Longshot(null),
          "Boosts indirect range by 2.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Gage(rules);
    }
  }

  public static final int RADIUS  = 2;
  public static final int POWER   = 20;
  public static final int DEFENSE = 10;

  public Gage(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = UnitModel.SEA | UnitModel.INDIRECT;

    addCommanderAbility(new Longshot(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class Longshot extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter rangeMod;

    protected Longshot(RuinedCommander commander)
    {
      super(commander, "Longshot");
      if( null == commander )
        return; // This isn't a "real" ability, just a scratch struct for the info page
      rangeMod = new UnitTypeFilter(new UnitIndirectRangeModifier(2));
      rangeMod.oneOf = commander.boostMaskAny;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(rangeMod);
    }
  }

}
