package CommandingOfficers.AW1.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Nell extends AW1Commander
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
      super("Nell", UIUtils.SourceGames.AW1, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Nell (AW1)\n"
          + "A respected CO of no mean ability.\n"
          + "An all-around competent CO. Good luck follows her wherever she goes.\n"
          + "(+10 max luck)\n"));
      infoPages.add(new InfoPage(new LuckyStar(null),
            "May become even luckier than normal.\n"
          + "(+40 max luck, for 0-59% more damage total.)\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: Willful cadets\n"
          + "Miss: Olaf"));
      infoPages.add(AW1_MECHANICS_BLURB);
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

    addCommanderAbility(new LuckyStar(this));
  }

  private static class LuckyStar extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lucky Star";
    private static final int COST = 6;
    UnitModifier luckMod;

    LuckyStar(Nell commander)
    {
      super(commander, NAME, COST);
      luckMod = new LuckModifier(40);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
    }
  }

}
