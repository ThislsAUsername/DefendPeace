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
import Terrain.MapMaster;
import Terrain.GameMap;

public class VonBolt extends AWBWCommander
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
      super("Von Bolt_BW", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Von Bolt (AWBW)\n"
          + "Units gain +10% attack and +10% defense.\n"));
      infoPages.add(new InfoPage(new ExMachina(null),
            "A 2-range missile deals 3HP damage and prevents all affected enemy units from acting next turn. The missile targets the opponents' greatest accumulation of unit value.\n"
          + "+10/10 stats (120/120 total)\n"
          + "Missile deals no friendly fire and thus its targeting ignores friendlies.\n"
          + "Missile ignores fog for targeting.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new VonBolt(rules);
    }
  }

  public VonBolt(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new ExMachina(this));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 10;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 10;
  }

  private static class ExMachina extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Ex Machina";
    private static final int COST = 10;
    private static final int POWER = 3;

    ExMachina(VonBolt commander)
    {
      super(commander, NAME, COST, new CostBasis(CHARGERATIO_FUNDS));
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams target = findTarget(map);
      GameEventQueue events = super.getEvents(map);

      target.power = POWER;
      target.inflictStun = true;
      target.selfHarm    = false;
      GameEvent event = target.getDamage(map, myCommander);
      events.add(event);

      return events;
    }
    private MeteorParams findTarget(GameMap map)
    {
      AWBWValueFinders.CostValueFinder finder = new AWBWValueFinders.CostValueFinder();
      finder.maxDamage = POWER;
      finder.selfHarm  = false;
      return MeteorParams.findValue(map, myCommander, 2, finder);
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
