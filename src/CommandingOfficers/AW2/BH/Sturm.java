package CommandingOfficers.AW2.BH;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Units.UnitContext;
import Terrain.MapMaster;
import Terrain.MapPerspective;
import Terrain.GameMap;

public class Sturm extends AW2Commander
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
      super("Sturm", UIUtils.SourceGames.AW2, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Sturm (AW2)\n"
          + "Commander of Black Hole's forces. A mysterious invader from another world.\n"
          + "All units have superior firepower. Movement cost is equal over all terrain types. Weak in the snow.\n"
          + "(+20/20 stats, perfect movement outside cold weather.)"));
      infoPages.add(new InfoPage(new MeteorStrike(null),
            "Pulls a giant meteor from space, which does 8 HP of damage to all affected units. Increases his units' firepower & defence (+20/30, 140/150 total).\n"
          + "Meteor has a 1/3 chance of targeting HP, funds, or funds with doubled indirects.\n"
          + "Meteor must center on a unit, and only counts damage to visible units.\n"));
      infoPages.add(new InfoPage(
            "Hit: Invasions\n"
          + "Miss: Peace"));
      infoPages.add(AW2_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sturm(rules);
    }
  }

  public Sturm(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new MeteorStrike(this));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 20;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 20;
  }
  @Override
  public void modifyMoveType(UnitContext uc)
  {
    SturmValueFinders.modifyMoveType(uc);
  }

  private static class MeteorStrike extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Meteor Strike";
    private static final int COST = 10;
    private static final int POWER = 8;
    UnitModifier fightMod;

    MeteorStrike(Sturm commander)
    {
      super(commander, NAME, COST, new CostBasis(CHARGERATIO_FUNDS));
      fightMod = new UnitFightStatModifier(20); // +10 from AW2Ability makes +20/30
      // Cart AI seems to only COP at start of turn
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(fightMod);
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
      SturmValueFinders.HPValueFinder hpFinder = new SturmValueFinders.HPValueFinder();
      hpFinder.countHidden = false;
      SturmValueFinders.CostValueFinder costFinder = new SturmValueFinders.CostValueFinder();
      costFinder.countHidden = false;
      SturmValueFinders.CostValueFinder artyFinder = new SturmValueFinders.CostValueFinder();
      artyFinder.countHidden = false;
      artyFinder.indirectMultiplier = 2;
      MapPerspective scoringMap = myCommander.army.myView;
      targets[0] = MeteorParams.planMeteorOnEnemy(map, scoringMap, myCommander, 2, hpFinder);
      targets[1] = MeteorParams.planMeteorOnEnemy(map, scoringMap, myCommander, 2, costFinder);
      targets[2] = MeteorParams.planMeteorOnEnemy(map, scoringMap, myCommander, 2, artyFinder);
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
