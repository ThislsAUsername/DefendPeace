package CommandingOfficers.Variants.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitCaptureModifier;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public class Sensei extends AWBWCommander
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
      super("Sensei", UIUtils.SourceGames.VARIANTS, UIUtils.YC, "ZAP");
      infoPages.add(new InfoPage(
            "Sensei (ZAP)\n"
          + "AW2 Sensei with AWBW rules and troop buffs instead of dudespawns.\n"
          + "Powerful infantry & high transport movement range. Superior firepower for copters, but weak vs. naval and vehicle units.\n"
          + "(+50/0 copters/footsoldiers, -10/0 non-air units)\n"));
      infoPages.add(new InfoPage(new CopterCommand(null, null),
            "Copters get +"+SenseiPower.MOVE_BUFF+" move and +25 attack (total 185/110)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new AirborneAssault(null, null),
            "COP, and reactivate footsoldiers with half capture power.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sensei(rules);
    }
  }

  public Sensei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new CopterCommand(this, cb));
    addCommanderAbility(new AirborneAssault(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.HOVER | UnitModel.TROOP) )
      params.attackPower += 50;
    if( params.attacker.model.isNone(UnitModel.AIR) )
      params.attackPower -= 10;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.baseCargoCapacity > 0 && uc.model.weapons.isEmpty() )
      uc.movePower += 1;
  }

  private static class SenseiPower extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    public static final int MOVE_BUFF = 1;
    UnitTypeFilter atkMod, movMod;

    SenseiPower(Sensei commander, String name, int cost, CostBasis basis)
    {
      super(commander, name, cost, basis);
      atkMod = new UnitTypeFilter(new UnitDamageModifier(25));
      atkMod.oneOf = UnitModel.HOVER;
      movMod = new UnitTypeFilter(new UnitMovementModifier(MOVE_BUFF));
      movMod.oneOf = UnitModel.HOVER;
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(atkMod);
      modList.add(movMod);
    }
  }

  private static class CopterCommand extends SenseiPower
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Copter Command";
    private static final int COST = 2;

    CopterCommand(Sensei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
    }
  }

  private static class AirborneAssault extends SenseiPower
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Airborne Assault";
    private static final int COST = 6;
    UnitTypeFilter capMod;

    AirborneAssault(Sensei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      capMod = new UnitTypeFilter(new UnitCaptureModifier(-50));
      capMod.oneOf = UnitModel.TROOP;
      AIFlags = PHASE_TURN_END;
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(atkMod);
      modList.add(movMod);
      modList.add(capMod);
    }
    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.army.getUnits() )
      {
        if( !unit.model.isAny(UnitModel.TROOP))
          continue;
        unit.isTurnOver = false;
      }
    }
  } // ~AirborneAssault
}
