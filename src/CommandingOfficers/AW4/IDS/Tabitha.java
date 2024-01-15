package CommandingOfficers.AW4.IDS;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.IValueFinder;
import CommandingOfficers.MeteorParams;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.*;

public class Tabitha extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Tabitha", UIUtils.SourceGames.AW4, UIUtils.IDS);
      infoPages.add(new InfoPage(
          "Belongs to the private military company IDS. A daughter of Dr. Caulder, she is extremely cruel and vindictive.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: 0\n"
        + "Zone Boost: All units +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new Firestorm(null),
          "Damages units over a wide area.\n"
        + Firestorm.POWER + " HP strike that targets HP with gunboats counting half.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Tabitha(rules);
    }
  }
  public static final int D2DREPAIRS = 5;
  public static final int RADIUS  = 0;
  public static final int POWER   = 50;
  public static final int DEFENSE = 50;

  public Tabitha(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);

    addCommanderAbility(new Firestorm(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  public static class FirestormValueFinder implements IValueFinder
  {
    int maxDamage = Firestorm.POWER;
    public int getValue(Commander attacker, Unit unit)
    {
      int health = unit.getHealth();
      if( health < 10 )
        return 1;
      health = Math.min(health, maxDamage * 10);

      if( 1 == unit.model.maxAmmo )
        health /= 2;

      if( !unit.CO.isEnemy(attacker) )
        return -health;
      return health;
    }
  }

  private static class Firestorm extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Firestorm";
    private static final int POWER = 8;

    Firestorm(Tabitha commander)
    {
      super(commander, NAME);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      MeteorParams target = findTarget(map);
      GameEventQueue events = super.getEvents(map);

      target.power = POWER;
      GameEvent event = target.getDamage(map, myCommander);
      events.add(event);

      return events;
    }
    private MeteorParams findTarget(GameMap map)
    {
      return MeteorParams.planMeteor(map, myCommander, 2, new FirestormValueFinder());
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
