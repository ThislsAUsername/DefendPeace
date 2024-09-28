package CommandingOfficers.AW2.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.LuckBadModifier;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Flak extends AW2Commander
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
      super("Flak", UIUtils.SourceGames.AW2, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Flak (AW2)\n"
          + "The strongman of the Black Hole army. Promoted from private by Hawke.\n"
          + "High firepower, but he relies solely on strength. His shoddy technique sometimes reduces the damage his units deal.\n"
          + "(+5 luck, 10 bad luck)"));
      infoPages.add(new InfoPage(new BruteForce(null, null),
            "Increases dispersion of fire. There is a chance of getting a super strong blow, but units' firepower might suddenly drop instead.\n"
          + "(+25/-10 luck, for 0-39% more damage and 0-19% less total)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new BarbaricBlow(null, null),
            "Attack power rises dramatically, but so does the dispersion rate, which affects the amount of damage targets take.\n"
          + "(+65/-20 luck, for 0-79% more damage and 0-29% less total)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Meat\n"
          + "Miss: Veggies"));
      infoPages.add(AW2_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Flak(rules);
    }
  }

  public Flak(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new BruteForce(this, cb));
    addCommanderAbility(new BarbaricBlow(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.luckRolled    += 5;
    params.luckRolledBad += 10;
  }

  private static class BruteForce extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Brute Force";
    private static final int COST = 3;
    UnitModifier luckMod, bunkMod;

    BruteForce(Flak commander, CostBasis basis)
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

  private static class BarbaricBlow extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Barbaric Blow";
    private static final int COST = 6;
    UnitModifier luckMod, bunkMod;

    BarbaricBlow(Flak commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      luckMod = new LuckModifier(65);
      bunkMod = new LuckBadModifier(20);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
      modList.add(bunkMod);
    }
  }

}
