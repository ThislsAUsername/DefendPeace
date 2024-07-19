package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Nell extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Nell", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Nell (AWBW)\n"
          + "Luck on attacks is +0 to +19%.\n"));
      infoPages.add(new InfoPage(
            "Lucky Star (3):\n"
          + "Luck is improved to +0% to +59%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Lady Luck (6):\n"
          + "Luck is improved to +0% to +99%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Nell(rules);
    }
  }

  public Nell(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    luck += 10;

    CostBasis cb = new CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new NellPower(this, cb, "Lucky Star", 3, 40));
    addCommanderAbility(new NellPower(this, cb, "Lady Luck",  6, 80));
  }

  private static class NellPower extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitModifier luckMod;

    NellPower(Nell commander, CostBasis basis, String name, int cost, int luck)
    {
      super(commander, name, cost, basis);
      luckMod = new LuckModifier(luck);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
    }
  }

}
