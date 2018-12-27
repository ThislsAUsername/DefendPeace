package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.GameMap;
import Units.Unit;

public class IDSCaulderSCOP extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Caulder\nSCOP", new instantiator());

  private static class instantiator implements COMaker
  {
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

  private static class SupremeBoost extends CommanderAbility
  {
    private static final String NAME = "Supreme Boost";
    private static final int COST = 5;

    SupremeBoost(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(40));
      myCommander.addCOModifier(new CODefenseModifier(25));
      for( Unit unit : myCommander.units )
      {
        double HP = unit.getPreciseHP();
        double maxHP = unit.model.maxHP;
        if( HP < maxHP )
        {
          int neededHP = (int) Math.min(maxHP - unit.getHP(), 5);
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
