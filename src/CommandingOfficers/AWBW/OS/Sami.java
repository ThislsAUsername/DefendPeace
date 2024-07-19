package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
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

public class Sami extends AWBWCommander
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
      super("Sami", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Sami (AWBW)\n"
          + "Footsoldiers gain +30% attack and a 50% capture point bonus (rounded down).\n"
          + "Other direct units lose -10% attack. Transports gain +1 movement.\n"));
      infoPages.add(new InfoPage(new DoubleTime(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Footsoldiers gain +1 movement and their attack is increased to +50% (160/110 total, +10 from AW2).\n"
          + "Their attack strength increases (+20, total 150) as well.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new VictoryMarch(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Footsoldiers gain +2 movement and their attack is increased to +70% (180/110 total).\n"
          + "Footsoldiers can capture buildings instantly (even with 1 HP).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
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

  private static class DoubleTime extends AWBWAbility
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

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(20)); // NOT -10 from AW2?!
      footAtkMod.oneOf = UnitModel.TROOP;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(footAtkMod);
    }
  }

  private static class VictoryMarch extends AWBWAbility
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

      footAtkMod = new UnitTypeFilter(new UnitDamageModifier(40)); // -10 from AW2
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
