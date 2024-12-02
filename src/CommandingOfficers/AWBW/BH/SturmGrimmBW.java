package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.AWBW.AWBWValueFinders;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import UI.UIUtils;
import Units.UnitContext;
import Terrain.GameMap;
import Terrain.MapMaster;

public class SturmGrimmBW extends AWBWCommander
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
      super("Sturm", UIUtils.SourceGames.AWBW, UIUtils.BH, "GBW");
      infoPages.add(new InfoPage(
            "Sturm (Grimm BW)\n"
          + "AWBW Sturm, but with Grimm's stats (matching AW1's D2D).\n"
          + "Movement cost over all terrain is reduced to 1, except in cold weather.\n"
          + "Units gain +30/-20 stats.\n"));
      infoPages.add(new InfoPage(
          METEOR_NAME+" ("+METEOR_COST+"):\n"
        + "A 2-range missile deals "+METEOR_POWER+"HP damage. The missile targets an enemy unit located at the greatest accumulation of unit value.\n"
        + "+10/10 stats.\n"));
      infoPages.add(new InfoPage(
          MEATIER_NAME+" ("+MEATIER_COST+"):\n"
        + "A 2-range missile deals "+MEATIER_POWER+"HP damage. The missile targets an enemy unit located at the greatest accumulation of unit value.\n"
        + "+10/10 stats.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new SturmGrimmBW(rules);
    }
  }

  private static final String METEOR_NAME = "Meteor Strike";
  private static final int METEOR_COST   =  6;
  private static final int METEOR_POWER  =  4;
  private static final int METEOR_FLAGS  =  0; // Probs not worth?
  private static final String MEATIER_NAME = "Meteor Strike II";
  private static final int MEATIER_COST  = 10;
  private static final int MEATIER_POWER =  8;
  private static final int MEATIER_FLAGS =  CommanderAbility.PHASE_TURN_START | CommanderAbility.PHASE_TURN_END;

  public SturmGrimmBW(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new MeteorStrike(this, METEOR_NAME , METEOR_COST , METEOR_POWER , METEOR_FLAGS , cb));
    addCommanderAbility(new MeteorStrike(this, MEATIER_NAME, MEATIER_COST, MEATIER_POWER, MEATIER_FLAGS, cb));
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
  @Override
  public void modifyMoveType(UnitContext uc)
  {
    SturmUtils.modifyMoveType(uc);
  }


  private static class MeteorStrike extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    public final int power;

    MeteorStrike(SturmGrimmBW commander, String name, int cost, int power, int flags, CostBasis basis)
    {
      super(commander, name, cost, basis);
      AIFlags = flags;
      this.power = power;
    }


    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams target = findTarget(map);
      GameEventQueue events = super.getEvents(map);

      target.power = power;
      GameEvent event = target.getDamage(map, myCommander);
      events.add(event);

      return events;
    }
    private MeteorParams findTarget(GameMap map)
    {
      AWBWValueFinders.CostValueFinder finder = new AWBWValueFinders.CostValueFinder();
      finder.maxDamage = power;
      MeteorParams meteor = MeteorParams.planMeteorOnEnemy(map, map, myCommander, 2, finder);
      if( meteor.target == null )
        meteor.target = new XYCoord(0, 0);
      return meteor;
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      MeteorParams target = findTarget(map);
      if( null != target.target ) addPopup(output, target.target, "COST");

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
