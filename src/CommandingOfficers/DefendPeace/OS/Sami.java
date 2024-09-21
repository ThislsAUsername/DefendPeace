package CommandingOfficers.DefendPeace.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.InstaCapModifier;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.Unit;
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
      super("Sami", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.OS, "ZAP");
      infoPages.add(new InfoPage(
            "Sami (nyoom)\n"
          + "AW2 Sami, but with lightning mechs and AWBW rules.\n"
          + "Footsoldiers +30 attack, 1.5x capture rate.\n"
          + "Mechs cost 2x, but get passive Lightning Strike.\n"
          + "Unarmed transports +1 move. -10/0 direct vehicle combat.\n"));
      infoPages.add(new InfoPage(new DoubleTime(null, null),
            "Infantry and mech units receive a movement bonus of 1 space.\n"
          + "Their attack strength increases (+20, total 150) as well.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new VictoryMarch(null, null),
            "Increases all foot soldiers' movement range by 2 spaces.\n"
          + "They can capture in one turn even if they're not at full HP.\n"
          + "(Footsoldier +50 attack, total 180)\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Chocolate\n"
          + "Miss: Cowards"));
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

  private ArrayList<Unit> dudesToReactivate = new ArrayList<Unit>();
  @Override
  protected void onTurnInit(MapMaster map, GameEventQueue events)
  {
    for( Unit u : units )
      if( u.model.isAny(UnitModel.MECH) )
        if( u.isTurnOver )
          u.isTurnOver = false;
        else
          dudesToReactivate.add(u);
  }
  // Mark units I will double-move
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    if( dudesToReactivate.contains(unit) )
      return 'S';
    return super.getUnitMarking(unit, activeArmy);
  }
  @Override
  public int getBuyCost(UnitModel um, XYCoord coord)
  {
    UnitContext uc = getCostContext(um, coord);
    if( uc.model.isAny(UnitModel.MECH) )
      uc.costRatio += 100;
    return uc.getCostTotal();
  }
  public GameEventQueue receiveMoveEvent(Unit unit, GamePath unitPath)
  {
    if( !dudesToReactivate.contains(unit) )
      return null;

    dudesToReactivate.remove(unit);
    unit.isTurnOver = false;
    return null;
  }
  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    if( !join.unitDonor.isTurnOver ) // We gave this dude his turn back after moving
      join.unitRecipient.isTurnOver = false;
    if( dudesToReactivate.contains(join.unitRecipient) )
      join.unitRecipient.isTurnOver = false;

    return null;
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
      modList.add(capMod);
    }
  }
}
