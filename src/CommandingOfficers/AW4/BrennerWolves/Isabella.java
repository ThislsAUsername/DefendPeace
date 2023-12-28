package CommandingOfficers.AW4.BrennerWolves;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModel;

public class Isabella extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Isabella", UIUtils.SourceGames.AW4, UIUtils.BW);
      infoPages.add(new InfoPage(
          "Has no memory of her past. Rescued by Will, she has joined the 12th Battalion – where she dreams of living in peace.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: All units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new DeepStrike(null),
          "Boosts mobility and indirect range by 2.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Isabella(rules);
    }
  }

  public static final int RADIUS  = 2;
  public static final int POWER   = 10;
  public static final int DEFENSE = 10;

  public Isabella(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = 0;
    this.boostMaskAll = UnitModel.LAND | UnitModel.DIRECT;

    addCommanderAbility(new DeepStrike(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class DeepStrike extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    UnitModifier moveMod;
    UnitModifier rangeMod;

    protected DeepStrike(RuinedCommander commander)
    {
      super(commander, "Deep Strike");
      if( null == commander )
        return; // This isn't a "real" ability, just a scratch struct for the info page
      moveMod = new UnitMovementModifier(2);
      rangeMod = new UnitIndirectRangeModifier(2);
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(rangeMod);
    }
  }

}
