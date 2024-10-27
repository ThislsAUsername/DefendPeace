package CommandingOfficers.DefendPeace.GE;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.StateTrackers.LightningUnitTracker;
import Engine.StateTrackers.StateTracker;
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
        + "AWBW Jess, but with lightning tanks.\n"
        + "+10 land vehicle attack. Other units -10 attack.\n"
        + "Land vehicles cost +70%, but get passive Lightning Strike."));
      infoPages.add(new InfoPage(TurboCharge(null, null),
          "Land vehicles +1 move, +10 attack. Fuel and ammo supplies are replenished.\n"
        + "+10 attack and defense. Also restores materials.\n"));
      infoPages.add(new InfoPage(Overdrive(null, null),
          "Land vehicles +2 move, +30 attack. Fuel and ammo supplies are replenished.\n"
        + "+10 attack and defense. Also restores materials.\n"));
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
    return new JessAbility(commander, cb, "Turbo Charge", 3, 1, 10);
  }
  private static CommanderAbility Overdrive(Commander commander, CostBasis cb)
  {
    return new JessAbility(commander, cb, "Overdrive", 6, 2, 30);
  }

  public Jess(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(TurboCharge(this, cb));
    addCommanderAbility(Overdrive(this, cb));
  }
  private LightningUnitTracker zapTracker;
  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    zapTracker = StateTracker.instance(game, LightningUnitTracker.class);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.TANK) )
      params.attackPower += 10;
    else
      params.attackPower -= 10;
  }
  @Override
  protected void onTurnInit(MapMaster map, GameEventQueue events)
  {
    zapTracker.resetFor(this);
    for( Unit u : units )
      if( u.model.isAll(UnitModel.TANK) )
        zapTracker.giveAction(u);
  }
  @Override
  public int getBuyCost(UnitModel um, XYCoord coord)
  {
    UnitContext uc = getCostContext(um, coord);
    if( uc.model.isAll(UnitModel.TANK) )
      uc.costRatio += 70;
    return uc.getCostTotal();
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
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      for( Unit unit : myCommander.army.getUnits() )
      {
        unit.resupply();
        unit.materials = unit.model.maxMaterials;
      }
    }
  }
}
