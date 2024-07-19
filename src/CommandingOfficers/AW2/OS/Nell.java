package CommandingOfficers.AW2.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Nell extends AW2Commander
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
      super("Nell", UIUtils.SourceGames.AW2, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Nell (AW2)\n"
          + "A competent Orange Star CO. Attended military school with Grit and Max.\n"
          + "Sometimes strikes with more force than expected. She’ll be the first to tell you she was born lucky.\n"
          + "(+10 max luck)\n"));
      infoPages.add(new InfoPage(
            "Lucky Star (3):\n"
          + "Improves her chance to strike with increased firepower, and damages multiple foes. Lucky!\n"
          + "(+40 max luck, for 0-59% more damage total.)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Lady Luck (6):\n"
          + "Improves her chance to strike with increased firepower, and damages even more foes. Lucky!\n"
          + "(+80 max luck, for 0-99% more damage total.)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Willful Students\n"
          + "Miss: Downtimes"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

  private static class NellPower extends AW2Ability
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
