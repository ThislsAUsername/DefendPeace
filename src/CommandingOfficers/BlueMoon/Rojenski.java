package CommandingOfficers.BlueMoon;

import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.WeaponModel;

public class Rojenski extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Rojenski");
      infoPages.add(new InfoPage(
          "--Rojenski (rebalanced Grit)--\r\n" +
          "  Indirects gain +1 range.\r\n" +
          "  +20% firepower in indirect combat, -20% firepower in direct combat.\r\n" +
          "xxxXXX\r\n" +
          "LONG SHOT: All indirects gain +1 range.\r\n" +
          "LONG BARREL: All indirects gain +2 range."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Rojenski(rules);
    }
  }
  
  public int indirectBuff = 20;

  public Rojenski(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      for( WeaponModel pewpew : um.weapons )
      {
        if( pewpew.maxRange > 1 )
        {
          pewpew.maxRange += 1;
        }
      }
    }

    addCommanderAbility(new RangeBonus(this, "Long Shot", 3, 1));
    addCommanderAbility(new RangeBonus(this, "Long Barrel", 6, 2));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange == 1 )
    {
      params.attackPower -= 20;
    }
    else
    {
      params.attackPower += indirectBuff;
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class RangeBonus extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private int power = 1;

    RangeBonus(Commander commander, String name, int cost, int buff)
    {
      super(commander, name, cost);
      power = buff;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new IndirectRangeBoostModifier(power));
    }
  }
}

