package CommandingOfficers.IDS;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.Unit;

public class Caulder extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Caulder");
      infoPages.add(new InfoPage(
          "--CAULDER--\r\n" +
          "All damaged units are repaired for +"+ D2DREPAIRS +" HP every turn (liable for costs).\r\n" +
          "XXXXX XXXXX\r\n" +
          "SUPREME BOOST: All units gain +40% firepower, +25% defense, and are repaired for +"+ SupremeBoost.REPAIRS +" HP (liable for costs -- watch your funds!)."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Caulder(rules);
    }
  }

  public static final int D2DREPAIRS = 2;

  public Caulder(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new SupremeBoost(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    GameEventQueue ret = super.initTurn(map);

    for( Unit unit : units )
    {
      double HP = unit.getPreciseHP();
      double maxHP = unit.model.maxHP;
      if( HP < maxHP )
      {
        int neededHP = (int) Math.min(maxHP - unit.getHP(), D2DREPAIRS);
        double proportionalCost = unit.model.getCost() / maxHP;
        int repairedHP = neededHP;
        while (money < repairedHP * proportionalCost)
        {
          repairedHP--;
        }
        money -= repairedHP * proportionalCost;
        unit.alterHP(repairedHP);

        // Top off HP if there's excess power but we hit the HP cap
        if (repairedHP < D2DREPAIRS && unit.getHP() == maxHP)
          unit.alterHP(1);
      }
    }
    
    return ret;
  }

  private static class SupremeBoost extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Supreme Boost";
    private static final int COST = 10;
    private static final int REPAIRS = 3;

    SupremeBoost(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(40));
      myCommander.addCOModifier(new CODefenseModifier(25));
      for( Unit unit : myCommander.units )
      {
        double HP = unit.getPreciseHP();
        double maxHP = unit.model.maxHP;
        if( HP < maxHP )
        {
          int neededHP = (int) Math.min(maxHP - unit.getHP(), REPAIRS);
          double proportionalCost = unit.model.getCost() / maxHP;
          int repairedHP = neededHP;
          while (myCommander.money < repairedHP * proportionalCost)
          {
            repairedHP--;
          }
          myCommander.money -= repairedHP * proportionalCost;
          unit.alterHP(repairedHP);

          // Top off HP if there's excess power but we hit the HP cap
          if (repairedHP < D2DREPAIRS && unit.getHP() == maxHP)
            unit.alterHP(1);
        }
      }
    }
  }
}

