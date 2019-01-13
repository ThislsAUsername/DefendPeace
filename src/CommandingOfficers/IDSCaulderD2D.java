package CommandingOfficers;

import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class IDSCaulderD2D extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Caulder\nD2D", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new IDSCaulderD2D();
    }
  }

  public IDSCaulderD2D()
  {
    super(coInfo);
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
        int neededHP = (int) Math.min(maxHP - unit.getHP(), 5);
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
}
