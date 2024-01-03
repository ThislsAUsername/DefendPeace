package CommandingOfficers.AW3.BH;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import CommandingOfficers.AW3.AW3ValueFinders;
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

public class VonBolt extends AW3Commander
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
      super("Von Bolt", UIUtils.SourceGames.AW3, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Von Bolt (AW3)\n"
          + "The new commander-in-chief of the Black Hole Forces. An old man who has been alive a very long time.\n"
          + "All units have superior firepower and defence (+10/10).\n"));
      infoPages.add(new InfoPage(new ExMachina(null),
            "Fires shock waves that paralyse and cause 3 HP of damage to all forces in range. Damaged units must skip their next turn.\n"
          + "+10/10 stats (120/120 total)\n"
          + "Shockwave has a 1/2 chance of targeting HP or funds.\n"
          + "Shockwave ignores fog for targeting.\n"));
      infoPages.add(new InfoPage(
            "Hit: Long life\n"
          + "Miss: Young 'uns!"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

  private static class ExMachina extends AW3Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Ex Machina";
    private static final int COST = 10;
    private static final int POWER = 3;

    ExMachina(VonBolt commander)
    {
      super(commander, NAME, COST, new CostBasis(CHARGERATIO_AW3));
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams[] targets = findTargets(map);
      int rand = map.game.getRN(targets.length);
      MeteorParams meteor = targets[rand];
      GameEventQueue events = super.getEvents(map);

      meteor.power = POWER;
      meteor.inflictStun = true;
      GameEvent event = meteor.getDamage(map, myCommander);
      events.add(event);

      return events;
    }
    private MeteorParams[] findTargets(GameMap map)
    {
      MeteorParams[] targets = new MeteorParams[2];
      targets[0] = MeteorParams.findValue(map, myCommander, 2, new AW3ValueFinders.HealthValueFinder());
      targets[1] = MeteorParams.findValue(map, myCommander, 2, new AW3ValueFinders.CostValueFinder());
      return targets;
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      MeteorParams[] targets = findTargets(map);
      if( null != targets[0].target ) addPopup(output, targets[0].target, "HP");
      if( null != targets[1].target ) addPopup(output, targets[1].target, "COST");

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
