package CommandingOfficers.Lazuria;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class Gage extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Gage");
      infoPages.add(new InfoPage(
          "--GAGE--\r\n" + 
          "Naval units and indirects gain +20% firepower and +10% defense.\r\n" + 
          "xxXXX\r\n" + 
          "LONG SHOT: All indirects gain +1 range.\r\n" + 
          "LONG BARREL: All indirects gain +2 range."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Gage(rules);
    }
  }

  public Gage(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isSeaUnit() | um.hasIndirectFireWeapon() )
      {
        um.modifyDamageRatio(20);
        um.modifyDefenseRatio(10);
      }
    }

    addCommanderAbility(new RangeBonus(this, "Long Shot", 2, 1));
    addCommanderAbility(new RangeBonus(this, "Long Barrel", 5, 2));
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

