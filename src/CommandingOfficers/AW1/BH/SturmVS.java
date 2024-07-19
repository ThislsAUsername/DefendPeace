package CommandingOfficers.AW1.BH;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.DamageMultiplierDefense;
import Engine.UnitMods.DamageMultiplierOffense;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Units.UnitContext;
import Terrain.MapMaster;
import Terrain.GameMap;

public class SturmVS extends AW1Commander
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
      super("Sturm", UIUtils.SourceGames.AW1, UIUtils.BH, "VS");
      infoPages.add(new InfoPage(
            "Sturm (AW1 VS)\n"
          + "A riddle within a shadow, revealing nothing.\n"
          + "???\n"
          + "(0.8x/0.8x damage dealt/taken while COP isn't active, perfect movement except in cold weather.)"));
      infoPages.add(new InfoPage(new MeteorStrike(null),
            "???\n"
          + "(0.9x/0.7x damage dealt/taken.)\n"
          + MeteorStrike.POWER+" HP meteor with a 1/3 chance of targeting HP, funds, or funds with doubled indirects.\n"
          + "Ignores fog for targeting purposes, but must center on a unit.\n"));
      infoPages.add(new InfoPage(
            "Hit: ???\n"
          + "Miss: ???"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new SturmVS(rules);
    }
  }

  public SturmVS(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new MeteorStrike(this));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( null != getActiveAbility() )
      return;
    params.attackerDamageMultiplier *=  80;
    params.attackerDamageMultiplier /= 100;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( null != getActiveAbility() )
      return;
    params.defenderDamageMultiplier *=  80;
    params.defenderDamageMultiplier /= 100;
  }
  @Override
  public void modifyMoveType(UnitContext uc)
  {
    SturmUtils.modifyMoveType(uc);
  }

  private static class MeteorStrike extends AW1Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Meteor Strike";
    private static final int COST = 10;
    private static final int POWER = 4;
    UnitModifier attMod;
    UnitModifier defMod;

    MeteorStrike(SturmVS commander)
    {
      super(commander, NAME, COST);
      attMod = new DamageMultiplierOffense(90);
      defMod = new DamageMultiplierDefense(70);
      // Cart AI seems to only COP at start of turn
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(attMod);
      modList.add(defMod);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams[] targets = findTargets(map);
      int rand = map.game.getRN(targets.length);
      MeteorParams meteor = targets[rand];
      GameEventQueue events = super.getEvents(map);

      meteor.power = POWER;
      GameEvent event = meteor.getDamage(map, myCommander);
      events.add(event);

      return events;
    }
    private MeteorParams[] findTargets(GameMap map)
    {
      MeteorParams[] targets = new MeteorParams[3];
      targets[0] = MeteorParams.planMeteorOnEnemy(map, map, myCommander, 2, new SturmUtils.HPValueFinder());
      targets[1] = MeteorParams.planMeteorOnEnemy(map, map, myCommander, 2, new SturmUtils.CostValueFinder());
      SturmUtils.CostValueFinder artyFinder = new SturmUtils.CostValueFinder();
      artyFinder.indirectMultiplier = 2;
      targets[2] = MeteorParams.planMeteorOnEnemy(map, map, myCommander, 2, artyFinder);
      return targets;
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      MeteorParams[] targets = findTargets(map);
      if( null != targets[0].target ) addPopup(output, targets[0].target, "HP");
      if( null != targets[1].target ) addPopup(output, targets[1].target, "COST");
      if( null != targets[2].target ) addPopup(output, targets[2].target, "ARTY");

      return output;
    }

    private void addPopup(ArrayList<DamagePopup> popups, XYCoord target, String callout)
    {
      for( DamagePopup popup : popups )
      {
        if( popup.coords.equals(target) )
        {
          popup.quantity += ", " + callout;
          return;
        }
      }
      popups.add(new DamagePopup(target, myCommander.myColor, callout));
    }
  }

}
