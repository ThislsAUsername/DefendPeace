package CommandingOfficers.AW3.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.LuckBadModifier;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Jugger extends AW3Commander
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
      super("Jugger", UIUtils.SourceGames.AW3, UIUtils.BH, "");
      infoPages.add(new InfoPage(
            "Jugger (AW3)\n"
          + "A robot-like CO with the Black Hole Army. No one knows his true identity. Gets a bit smarter when he uses a CO Power.\n"
          + "High firepower, but he relies solely on strength. His shoddy technique sometimes reduces the damage his units deal.\n"
          + "(+20 luck, 15 bad luck)"));
      infoPages.add(new InfoPage(new Overclock(null, null),
            "Increases dispersion of fire. There is a chance of getting a superstrong blow, but units' firepower might suddenly drop instead.\n"
          + "(+25/-10 luck, for 0-54% more damage and 0-24% less total)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new SystemCrash(null, null),
            "Attack power rises dramatically, but so does the dispersion rate, which affects the amount of damage targets take.\n"
          + "(+65/-30 luck, for 0-94% more damage and 0-44% less total)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Energy\n"
          + "Miss: Static electricity"));
      infoPages.add(AW3_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jugger(rules);
    }
  }

  public Jugger(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Overclock(this, cb));
    addCommanderAbility(new SystemCrash(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.luckRolled    += 20;
    params.luckRolledBad += 15;
  }

  private static class Overclock extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Overclock";
    private static final int COST = 3;
    UnitModifier luckMod, bunkMod;

    Overclock(Jugger commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      luckMod = new LuckModifier(25);
      bunkMod = new LuckBadModifier(10);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
      modList.add(bunkMod);
    }
  }

  private static class SystemCrash extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "System Crash";
    private static final int COST = 7;
    UnitModifier luckMod, bunkMod;

    SystemCrash(Jugger commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      luckMod = new LuckModifier(65);
      bunkMod = new LuckBadModifier(30);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
      modList.add(bunkMod);
    }
  }

}
