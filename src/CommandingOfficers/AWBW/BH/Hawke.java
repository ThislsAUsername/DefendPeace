package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.GameEvents.MassHealEvent;
import UI.UIUtils;
import Units.Unit;
import lombok.var;
import Terrain.MapMaster;

public class Hawke extends AWBWCommander
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
      super("Hawke", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Hawke (AWBW)\n"
          + "Units gain +10% attack."));
      infoPages.add(new InfoPage(
            "Black Wave (5):\n"
          + "All units gain +1 HP, and all enemy units lose -1 HP (to a minimum of 0.1 HP).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Black Storm (9):\n"
          + "All units gain +2 HP, and all enemy units lose -2 HP (to a minimum of 0.1 HP).\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Black coffee\n"
          + "Miss: Incompetence"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Hawke(rules);
    }
  }

  private static final int BS_FLAGS =  CommanderAbility.PHASE_TURN_START | CommanderAbility.PHASE_TURN_END;

  public Hawke(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new VampiricDamage(this, "Black Wave" , 5, 10, 0, cb));
    addCommanderAbility(new VampiricDamage(this, "Black Storm", 9, 20, BS_FLAGS, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 10;
  }

  private static class VampiricDamage extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    int power;

    VampiricDamage(Hawke commander, String name, int cost, int power, int flags, CostBasis basis)
    {
      super(commander, name, cost, basis);
      AIFlags = flags;
      this.power = power;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      ArrayList<Unit> victims = new ArrayList<>();
      ArrayList<Unit> friends = new ArrayList<>();

      for( int y = 0; y < map.mapHeight; y++ )
      {
        for( int x = 0; x < map.mapWidth; x++ )
        {
          Unit resi = map.getResident(x, y);
          if( resi != null )
          {
            if( myCommander.army == resi.CO.army )
              friends.add(resi);
            else if( myCommander.isEnemy(resi.CO) )
              victims.add(resi);
          }
        }
      }
      var heal = new MassHealEvent(null, friends, power);
      heal.roundUp = false;
      var damage = new MassDamageEvent(myCommander, victims, power, false);

      GameEventQueue events = new GameEventQueue();
      events.add(heal);
      events.add(damage);

      return events;
    }
  }

}
