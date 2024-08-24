package CommandingOfficers.AW2.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
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

public class Sami extends AW2Commander
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
      super("Sami", UIUtils.SourceGames.AW2, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Sami (AW2)\n"
          + "A strong-willed soldier who backs up Max and Andy. A graduate of special-forces training.\n"
          + "Being an infantry specialist, her foot soldiers move farther, capture faster, and cause more damage. Weak vs. vehicles.\n"
          + "(Footsoldiers +30 attack, 1.5x capture rate.)\n"
          + "(Unarmed transports +1 move. -10/0 direct vehicle combat.)\n"));
      infoPages.add(new InfoPage(new DoubleTime(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Infantry and mech units receive a movement bonus of 1 space.\n"
          + "Their attack strength increases (+20, total 150) as well.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new VictoryMarch(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Increases all foot soldiers' movement range by 2 spaces.\n"
          + "They can capture in one turn even if they're not at full HP.\n"
          + "(Footsoldier +50 attack, total 180)\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Chocolate\n"
          + "Miss: Cowards"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new DoubleTime(this, cb));
    addCommanderAbility(new VictoryMarch(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange < 2 && params.attacker.model.isNone(UnitModel.TROOP) )
      params.attackPower -= 10;
    if( params.attacker.model.isAny(UnitModel.TROOP) )
      params.attackPower += 30;
  }
  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    uc.capturePower += 50;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.baseCargoCapacity > 0 && uc.model.weapons.isEmpty() )
      uc.movePower += 1;
  }

  private static class DoubleTime extends AW2Ability
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

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(20));
      footAtkMod.oneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(footAtkMod);
    }
  }

  private static class VictoryMarch extends AW2Ability
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

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(50));
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
