package CommandingOfficers.AW3.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import CommandingOfficers.CommanderAbility.CostBasis;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.InstaCapModifier;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitContext;
import Units.UnitModel;

public class Sami extends AW3Commander
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
      super("Sami", UIUtils.SourceGames.AW3, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Sami (AW3)\n"
          + "A strong-willed Orange Star special forces captain who loves long hair.\n"
          + "As an infantry specialist, her foot soldiers do more damage (+20%) and capture faster (1.5x). Non-infantry direct combat units have weaker firepower (-10%).\n"
          + "(No transport boost)\n"));
      infoPages.add(new InfoPage(new DoubleTime(null, new CostBasis(CHARGERATIO_AW3)),
            "Infantry units receive a movement bonus of one space. Their attack also increases (+30, 160 total).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new VictoryMarch(null, new CostBasis(CHARGERATIO_AW3)),
            "Increases all foot soldiers’ movement by two spaces and gives them an attack bonus (+60, 190 total).\n"
          + "They capture in one turn even below full HP.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Chocolate\n"
          + "Miss: Cowards"));
      infoPages.add(AW3_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sami(rules);
    }
  }

  public Sami(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new DoubleTime(this, cb));
    addCommanderAbility(new VictoryMarch(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 && params.attacker.model.isNone(UnitModel.TROOP) )
      params.attackPower -= 10;
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      params.attackPower += 20;
  }
  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    uc.capturePower += 50;
  }

  private static class DoubleTime extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Double Time";
    private static final int COST = 3;
    UnitTypeFilter moveMod, footAtkMod;

    DoubleTime(Sami commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(1));
      moveMod.oneOf = UnitModel.TROOP;

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(30));
      footAtkMod.oneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(footAtkMod);
    }
  }

  private static class VictoryMarch extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Victory March";
    private static final int COST = 8;
    UnitTypeFilter moveMod, footAtkMod, capMod;

    VictoryMarch(Sami commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitTypeFilter(new UnitMovementModifier(2));
      moveMod.oneOf = UnitModel.TROOP;

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(60));
      footAtkMod.oneOf = UnitModel.TROOP;

      capMod = new UnitTypeFilter(new InstaCapModifier());
      capMod.oneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(footAtkMod);
      modList.add(footAtkMod);
    }
  }
}
