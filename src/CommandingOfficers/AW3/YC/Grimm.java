package CommandingOfficers.AW3.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import CommandingOfficers.CommanderInfo.InfoPage;
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
          + "A CO with a dynamic personality. Couldn't care less about the details. Nicknamed “Lightning Grimm”.\n"
          + "Firepower of all units is increased (+30) due to his daredevil nature, but their defence is a little weak (-20).\n"));
      infoPages.add(new InfoPage(new Knuckleduster(null, null),
            "Increases the attack (+20) of all units.\n"
          + "+10 attack and defense (160/90)\n"));
      infoPages.add(new InfoPage(new Haymaker(null, null),
            "Greatly increases the attack (+50) of all units.\n"
          + "+10 attack and defense (190/90)\n"));
      infoPages.add(new InfoPage(
            "Hit: Doughnuts\n"
          + "Miss: Planning"));
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

  private static class Haymaker extends AW3Ability
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
