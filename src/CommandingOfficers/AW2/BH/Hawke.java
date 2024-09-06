package CommandingOfficers.AW2.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.GameEvents.MassHealEvent;
import UI.UIUtils;
import Units.Unit;
import lombok.var;
import Terrain.MapMaster;

public class Hawke extends AW2Commander
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
      super("Hawke", UIUtils.SourceGames.AW2, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Hawke (AW2)\n"
          + "The leader of Black Hole's 4 COs. He'll stop at nothing to achieve his goals.\n"
          + "All units possess superior firepower (+10). His CO Power builds up more slowly than those of the other COs.\n"));
      infoPages.add(new InfoPage(
            "Black Wave (5):\n"
          + "All (non-loaded) damaged units recover 1 HP (non-rounding). In addition, all enemy units suffer 1 HP of damage.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Black Storm (9):\n"
          + "All (non-loaded) damaged units recover 2 HP (non-rounding). In addition, all enemy units suffer 2 HP of damage.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Black coffee\n"
          + "Miss: Incompetence"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

  private static class VampiricDamage extends AW2Ability
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
          if( resi != null)
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
