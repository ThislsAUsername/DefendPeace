package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.LuckBadModifier;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Flak extends AWBWCommander
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
      super("Flak", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Flak (AWBW)\n"
          + "Luck on attacks is -9% to +24%."));
      infoPages.add(new InfoPage(new BruteForce(null, null),
            "Luck range is changed to -19% to +49%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new BarbaricBlow(null, null),
            "Luck range is changed to -39% to +89%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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
    params.luckRolled    += 15;
    params.luckRolledBad += 10;
  }

  private static class BruteForce extends AWBWAbility
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

  private static class BarbaricBlow extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Barbaric Blow";
    private static final int COST = 6;
    UnitModifier luckMod, bunkMod;

    BarbaricBlow(Flak commander, CostBasis basis)
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
