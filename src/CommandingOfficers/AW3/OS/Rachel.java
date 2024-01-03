package CommandingOfficers.AW3.OS;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW3.AW3Commander;
import CommandingOfficers.AW3.AW3ValueFinders;
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

public class Rachel extends AW3Commander
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
      super("Rachel", UIUtils.SourceGames.AW3, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Rachel (AW3)\n"
          + "A young Orange Star CO. She strives to follow in the footsteps of her big sister, Nell.\n"
          + "Her troops are quite hardworking and they increase base resupplies by one.\n"));
      infoPages.add(new InfoPage(new LuckyLass(null, new CostBasis(CHARGERATIO_AW3)),
            "Improves her chance to strike with massive firepower and destroy her enemies!\n"
          + "Lucky!\n"
          + "+30 max luck, +10/10 stats.\n"));
      infoPages.add(new InfoPage(new CoveringFire(null, new CostBasis(CHARGERATIO_AW3)),
            "Provides covering fire by launching three missiles from Orange Star HQ in Omega Land.\n"
          + "+10/10 stats\n"
          + "Fires missiles in order at infantry HP, funds, and HP.\n"
          + "Shockwave ignores fog for targeting.\n"));
      infoPages.add(new InfoPage(
            "Hit: Hard Work\n"
          + "Miss: Excuses"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_AW3);
    addCommanderAbility(new LuckyLass(this, cb));
    addCommanderAbility(new CoveringFire(this, cb));
  }

  @Override
  public int getRepairPower()
  {
    return 3;
  }

  private static class LuckyLass extends AW3Ability
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
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      super.enqueueUnitMods(gameMap, modList);
      modList.add(luckMod);
    }
  }

  private static class CoveringFire extends AW3Ability
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
      // Should targeting account for the previous hits' damage?
      MeteorParams[] targets = new MeteorParams[3];
      targets[0] = MeteorParams.findValue(map, myCommander, 2, new AW3ValueFinders.CaptureValueFinder());
      targets[1] = MeteorParams.findValue(map, myCommander, 2, new AW3ValueFinders.CostValueFinder());
      targets[2] = MeteorParams.findValue(map, myCommander, 2, new AW3ValueFinders.HealthValueFinder());
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
