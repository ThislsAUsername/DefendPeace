package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.AWBW.AWBWValueFinders;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.LuckModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.GameMap;
import Terrain.MapMaster;

public class Rachel extends AWBWCommander
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
      super("Rachel", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Rachel (AWBW)\n"
          + "Units repair +1 additional HP (note: liable for costs).\n"));
      infoPages.add(new InfoPage(new LuckyLass(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Luck is improved to +0% to +39%.\n"
          + "(+30 max luck, +10/10 stats.)\n"));
      infoPages.add(new InfoPage(new CoveringFire(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Three 2-range missiles deal 3HP damage each. The missiles target the opponents' greatest accumulation of footsoldier HP, unit value, and unit HP (in that order).\n"
          + "+10/10 stats\n"
          + "Infantry missile ignores friendly fire and all non-footsoldier units.\n"
          + "Missiles ignore fog for targeting.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Rachel(rules);
    }
  }

  public Rachel(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new LuckyLass(this, cb));
    addCommanderAbility(new CoveringFire(this, cb));
  }

  @Override
  public int getRepairPower()
  {
    return 3;
  }

  private static class LuckyLass extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lucky Lass";
    private static final int COST = 3;
    UnitModifier luckMod;

    LuckyLass(Rachel commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      AIFlags = 0; // None of our AIs really know how to use luck
      luckMod = new LuckModifier(30);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(luckMod);
    }
  }

  private static class CoveringFire extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Covering Fire";
    private static final int COST = 6;

    CoveringFire(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams[] targets = findTargets(map);
      GameEventQueue events = super.getEvents(map);

      for( MeteorParams meteor : targets )
      {
        GameEvent event = meteor.getDamage(map, myCommander);
        events.add(event);
      }

      return events;
    }
    private MeteorParams[] findTargets(GameMap map)
    {
      // Targeting does not account for previous hits' damage
      MeteorParams[] targets = new MeteorParams[3];
      targets[0] = MeteorParams.planMeteor(map, myCommander, 2, new AWBWValueFinders.CaptureValueFinder());
      targets[1] = MeteorParams.planMeteor(map, myCommander, 2, new AWBWValueFinders.CostValueFinder());
      targets[2] = MeteorParams.planMeteor(map, myCommander, 2, new AWBWValueFinders.HealthValueFinder());
      for( MeteorParams silo : targets )
        if( silo.target == null )
          silo.target = new XYCoord(0, 0);

      return targets;
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      MeteorParams[] targets = findTargets(map);
      if( null != targets[0].target ) addPopup(output, targets[0].target, "INF");
      if( null != targets[1].target ) addPopup(output, targets[1].target, "COST");
      if( null != targets[2].target ) addPopup(output, targets[2].target, "HP");

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
