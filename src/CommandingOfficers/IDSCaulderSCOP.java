package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class IDSCaulderSCOP extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Caulder", new instantiator());

  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "--CAULDER--\r\n" + 
          "All damaged units are repaired for +2 HP every turn (liable for costs).\r\n" + 
          "XXXXX XXXXX\r\n" + 
          "SUPREME BOOST: All units gain +40% firepower, +25% defense, and are repaired for +3 HP (liable for costs)."));
    }
    @Override
    public Commander create()
    {
      return new IDSCaulderSCOP();
    }
  }

  public IDSCaulderSCOP()
  {
    super(coInfo);

    addCommanderAbility(new SupremeBoost(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    GameEventQueue ret = super.initTurn(map);

    for( Unit unit : units )
    {
      double HP = unit.getPreciseHP();
      double maxHP = unit.model.maxHP;
      if( HP < maxHP )
      {
        int neededHP = (int) Math.min(maxHP - unit.getHP(), 2);
        double proportionalCost = unit.model.getCost() / maxHP;
        int repairedHP = neededHP;
        while (money < repairedHP * proportionalCost)
        {
          repairedHP--;
        }
        money -= repairedHP * proportionalCost;
        unit.alterHP(repairedHP);
      }
    }
    
    return ret;
  }

  private static class SupremeBoost extends CommanderAbility
  {
    private static final String NAME = "Supreme Boost";
    private static final int COST = 10;

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
          int neededHP = (int) Math.min(maxHP - unit.getHP(), 3);
          double proportionalCost = unit.model.getCost() / maxHP;
          int repairedHP = neededHP;
          while (myCommander.money < repairedHP * proportionalCost)
          {
            repairedHP--;
          }
          myCommander.money -= repairedHP * proportionalCost;
          unit.alterHP(repairedHP);
        }
      }
    }
  }
}
