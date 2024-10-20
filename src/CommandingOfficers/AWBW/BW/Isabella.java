package CommandingOfficers.AWBW.BW;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitIndirectRangeModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Isabella extends AWBWCommander
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
      super("Isabella", UIUtils.SourceGames.AWBW, UIUtils.BW);
      infoPages.add(new InfoPage(
            "Isabella (AWBW)\n"
          + "Units gain +10% attack\n"));
      infoPages.add(new InfoPage(new DeepStrike(null, null),
            "All units gain +1 movement; all indirects also gain +1 range.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new Overlord(null, null),
            "All units gain +2 movement and +10% defense; all indirects also gain +2 range.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Isabella(rules);
    }
  }

  public Isabella(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new DeepStrike(this, cb));
    addCommanderAbility(new Overlord(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 10;
  }

  private static class DeepStrike extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Deep Strike";
    private static final int COST = 3;
    UnitModifier moveMod, shotMod;
    
    DeepStrike(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitMovementModifier(1);
      shotMod = new UnitIndirectRangeModifier(1);
    }
    
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(shotMod);
    }
  }

  private static class Overlord extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Overlord";
    private static final int COST = 7;
    UnitModifier statMod, moveMod, shotMod;

    Overlord(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDefenseModifier(10);
      moveMod = new UnitMovementModifier(2);
      shotMod = new UnitIndirectRangeModifier(2);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(moveMod);
      modList.add(shotMod);
    }
  }

}
