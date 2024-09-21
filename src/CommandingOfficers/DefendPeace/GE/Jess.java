package CommandingOfficers.DefendPeace.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public class Jess extends AWBWCommander
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
      super("Jess", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.GE, "ZAP");
      infoPages.add(new InfoPage(
          "Jess (nyoom)\n"
        + "AWBW Jess, but with lightning vehicles and AWBW rules.\n"
        + "+10 land vehicle attack. Other units -10 attack.\n"
        + "Non-infantry cost 2x, but get passive Lightning Strike."));
      infoPages.add(new InfoPage(TurboCharge(null, null),
          "Movement range of (land) vehicles increases by 1 space. Firepower increases (+20), and fuel and ammo supplies are also replenished.\n"
        + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(Overdrive(null, null),
          "Increase in the attack strength of vehicular (land) units (+40) and 2-space increase in movement range. Also restores fuel and ammo supplies.\n"
        + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jess(rules);
    }
  }
  private static CommanderAbility TurboCharge(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Turbo Charge", 3, 1, 20);
  }
  private static CommanderAbility Overdrive(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Overdrive", 6, 2, 40);
  }

  public Jess(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(TurboCharge(this, cb));
    addCommanderAbility(Overdrive(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.TANK) )
      params.attackPower += 10;
    else
      params.attackPower -= 10;
  }
  private ArrayList<Unit> dudesToReactivate = new ArrayList<Unit>();
  @Override
  protected void onTurnInit(MapMaster map, GameEventQueue events)
  {
    for( Unit u : units )
      if( u.model.isNone(UnitModel.TROOP) )
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
    if( uc.model.isNone(UnitModel.TROOP) )
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


  private static class JessAbility extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter moveMod, attMod;

    JessAbility(Commander commander, CostBasis cb, String name, int cost, int tankMove, int tankAttack)
    {
      super(commander, name, cost, cb);
      AIFlags = PHASE_TURN_START;
      moveMod = new UnitTypeFilter(new UnitMovementModifier(tankMove));
      moveMod.allOf = UnitModel.TANK;
      attMod  = new UnitTypeFilter(new UnitDamageModifier(tankAttack));
      attMod.allOf = UnitModel.TANK;
    }

    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(attMod);
    }

    @Override
    protected void perform(MapMaster map)
    {
      super.perform(map);
      for( Unit u : myCommander.army.getUnits() )
        u.resupply();
    }
  }
}
