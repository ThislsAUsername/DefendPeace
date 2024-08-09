package CommandingOfficers.AWBW.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Grimm extends AWBWCommander
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
      super("Grimm", UIUtils.SourceGames.AWBW, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Grimm (AWBW)\n"
          + "Units gain +30% attack, but lose 20% defense.\n"));
      infoPages.add(new InfoPage(new Knuckleduster(null, null),
            "All units' attack is increased to +50%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new Haymaker(null, null),
            "All units' attack is increased to +80%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Grimm(rules);
    }
  }

  public Grimm(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Knuckleduster(this, cb));
    addCommanderAbility(new Haymaker(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 30;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction -= 20;
  }

  private static class Knuckleduster extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Knuckleduster";
    private static final int COST = 3;
    UnitModifier statMod;

    Knuckleduster(Grimm commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDamageModifier(20);
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
    }
  }

  private static class Haymaker extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Haymaker";
    private static final int COST = 6;
    UnitModifier atkMod;

    Haymaker(Grimm commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      atkMod = new UnitDamageModifier(50);
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(atkMod);
    }
  }

}
