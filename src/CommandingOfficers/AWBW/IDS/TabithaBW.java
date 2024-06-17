package CommandingOfficers.AWBW.IDS;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.MeteorParams;
import CommandingOfficers.AW4.IDS.Tabitha.FirestormValueFinder;
import CommandingOfficers.AWBW.AWBWCommander;
import CommandingOfficers.AWBW.COUableCommander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.Unit;

public class TabithaBW extends COUableCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Tabitha", UIUtils.SourceGames.AWBW, UIUtils.IDS);
      infoPages.add(new InfoPage(
          "Can grant a single Mega Boost of +"+POWER+"/"+DEFENSE+" stats.\n"
        + "This powerup lasts until the unit is destroyed.\n"));
      infoPages.add(new InfoPage(
          "Firestorm (6):\n"
        + "4 HP strike that targets HP with gunboats counting half.\n"
        + "Unboosted units get +20/20 stats.\n"
        + "All units get +10/10 stats."));
      infoPages.add(new InfoPage(
          "Apocalypse (10):\n"
        + "8 HP strike that targets HP with gunboats counting half.\n"
        + "Unboosted units get +"+25+"/"+25+" stats.\n"
        + "All units get +10/10 stats."));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(AWBWCommander.AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new TabithaBW(rules);
    }
  }
  public static final int POWER   = 50;
  public static final int DEFENSE = 35;

  public TabithaBW(GameScenario.GameRules rules)
  {
    super(POWER, DEFENSE, coInfo, rules);
    deployCostPercent = 50;

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new NukeIt(this, "Firestorm",   6, cb, 4, 10, 10));
    addCommanderAbility(new NukeIt(this, "Apocalypse", 10, cb, 8, 25, 25));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue onCOULost(Unit minion)
  {
    modifyAbilityStars(-42);
    return null;
  }

  private static class NukeIt extends NonStackingBoost
  {
    private static final long serialVersionUID = 1L;
    public final int nukePower;

    NukeIt(TabithaBW commander, String name, int cost, CostBasis basis, int nuke, int pAtk, int pDef)
    {
      super(commander, name, cost, basis, pAtk, pDef);
      nukePower = nuke;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams target = findTarget(map);
      GameEventQueue events = super.getEvents(map);

      GameEvent event = target.getDamage(map, myCommander);
      events.add(event);

      return events;
    }
    private MeteorParams findTarget(GameMap map)
    {
      FirestormValueFinder hitFinder = new FirestormValueFinder();
      hitFinder.maxDamage = nukePower;
      MeteorParams hitFound = MeteorParams.planMeteor(map, myCommander, 2, hitFinder);
      hitFound.power = nukePower;
      return hitFound;
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      MeteorParams target = findTarget(map);
      if( null != target.target ) addPopup(output, target.target, "POW");

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
