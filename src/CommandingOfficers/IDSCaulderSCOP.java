package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class IDSCaulderSCOP extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Caulder\n2&SCOP", new instantiator());

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

  @Override
  public void initTurn(GameMap map)
  {
    super.initTurn(map);

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
  }

  private static class SupremeBoost extends CommanderAbility
  {
    private static final String NAME = "Supreme Boost";
    private static final int COST = 8;

    SupremeBoost(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(40));
      myCommander.addCOModifier(new CODefenseModifier(25));
    }
  }
}
