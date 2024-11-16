package CommandingOfficers.AWBW.BW;

import java.util.ArrayList;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.AWBW.COUableCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import Engine.UnitMods.VisionModifier;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModel;

public class Lin extends COUableCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Lin", UIUtils.SourceGames.AWBW, UIUtils.BW);
      infoPages.add(new InfoPage(
          "Can grant one ground unit +"+POWER+"/"+DEFENSE+" stats each turn."));
      infoPages.add(new InfoPage(
          "Scout (2):\n"
        + "Ground units gain +1 vision and can see into hiding places.\n"
        + "All units get +10/10 stats."));
      infoPages.add(new InfoPage(
          "Night Vision (5):\n"
        + "Ground units gain +2 vision and can see into hiding places.\n"
        + "Unboosted units get +"+POWER+"/"+DEFENSE+" stats.\n"
        + "All units get +10/10 stats."));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(AWBWCommander.AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Lin(rules);
    }
  }
  public static final int POWER   = 20;
  public static final int DEFENSE = 20;

  public Lin(GameScenario.GameRules rules)
  {
    super(POWER, DEFENSE, coInfo, rules);
    canDeployMask = UnitModel.LAND;
    resetCOUsEveryTurn = true; // Deploy anywhere every turn

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new LinPower(this, "Scout",        2, cb, 1, 0, 0));
    addCommanderAbility(new LinPower(this, "Night Vision", 5, cb, 2, POWER, DEFENSE));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class LinPower extends NonStackingBoost
  {
    private static final long serialVersionUID = 1L;
    final UnitTypeFilter sightMod;

    LinPower(Lin commander, String name, int cost, CostBasis basis, int sight, int pAtk, int pDef)
    {
      super(commander, name, cost, basis, pAtk, pDef);
      sightMod = new UnitTypeFilter(new VisionModifier(sight));
      sightMod.oneOf = commander.canDeployMask;
      // stat boost is filtered by parent on our DeployMask already.
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      myCommander.army.myView.revealFog();
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(sightMod);
      super.enqueueMods(gameMap, modList);
    }
  }
}
