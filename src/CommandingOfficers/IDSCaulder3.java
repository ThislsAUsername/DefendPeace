package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class IDSCaulder3 extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Caulder Alt");
      infoPages.add(new InfoPage(
          "--CAULDER ALT--\r\n" +
          "All damaged units are repaired for +"+ D2DREPAIRS +" HP every turn (liable for costs).\r\n" +
          "XXXXX XXXXX\r\n" +
          "SUPREME BOOST: All units gain +40% firepower, +25% defense, and are repaired for +"+ SupremeBoost.REPAIRS +" HP (liable for costs -- watch your funds!)."));
    }
    @Override
    public Commander create()
    {
      return new IDSCaulder3();
    }
  }

  public static final int D2DREPAIRS = 3;

  public IDSCaulder3()
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
    private static final String NAME = "Supreme Boost";
    private static final int COST = 10;
    private static final int REPAIRS = 2;

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

