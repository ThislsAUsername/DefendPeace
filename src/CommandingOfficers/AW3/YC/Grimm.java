package CommandingOfficers.AW3.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Grimm extends AW3Commander
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
      super("Grimm", UIUtils.SourceGames.AW3, UIUtils.YC, "");
      infoPages.add(new InfoPage(
            "Grimm (AWDS)\n"
          + "\n"
          + "(+30/30 stats for 1.2x prices)\n"));
      infoPages.add(new InfoPage(new Knuckleduster(null, null),
            "\n"
          + "(+20/10 stats, total 150/140)\n"));
      infoPages.add(new InfoPage(new BeegPunch(null, null),
            "\n"
          + "(+20/30 stats, total 150/160; +65 attack on counters)\n"));
      infoPages.add(AW3_MECHANICS_BLURB);
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
    addCommanderAbility(new BeegPunch(this, cb));
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

  private static class Knuckleduster extends AW3Ability
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

  private static class BeegPunch extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "";
    private static final int COST = 6;
    UnitModifier atkMod;

    BeegPunch(Grimm commander, CostBasis basis)
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
