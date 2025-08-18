package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.LuckBaseModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class NellWarCrimes extends AWBWCommander
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
      super("Nell", UIUtils.SourceGames.AWBW, UIUtils.OS, "crime");
      infoPages.add(new InfoPage(
            "Nell (warcrimes)\n"
          + "Nell, but she always rolls max luck.\n"
          + "Luck on attacks is +19%.\n"));
      infoPages.add(new InfoPage(
            "Lucky Star (3):\n"
          + "Luck is improved to +59%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Lady Luck (6):\n"
          + "Luck is improved to +99%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new NellWarCrimes(rules);
    }
  }

  public NellWarCrimes(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
    luck = 0;

    CostBasis cb = new CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new NellPower(this, cb, "Lucky Star", 3, 40));
    addCommanderAbility(new NellPower(this, cb, "Lady Luck",  6, 80));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
      params.luckBase += 19;
  }

  private static class NellPower extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitModifier luckMod;

    NellPower(NellWarCrimes commander, CostBasis basis, String name, int cost, int luck)
    {
      super(commander, name, cost, basis);
      luckMod = new LuckBaseModifier(luck);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
    }
  }

}
