package CommandingOfficers.BlueMoon;

import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.WeaponModel;

public class Grit extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Grit");
      infoPages.add(new InfoPage(
          "Grit\r\n" +
           "  Indirects gain +1 range.\r\n" +
           "  +20% firepower in indirect combat\r\n" +
           "  -20% firepower in non-footsoldier direct combat.\r\n" +
          "Snipe Attack -- Indirect units gain +1 Range and +20% attack\r\n" + 
          "Super Snipe -- Indirect units gain +2 Range and +20% attack"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Grit(rules);
    }
  }
  
  public int indirectBuff = 20;

  public Grit(GameScenario.GameRules rules)
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

    addCommanderAbility(new SnipeAttack(this));
    addCommanderAbility(new SuperSnipe(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.indirectBuff = 20;
    return super.initTurn(map);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange == 1 && params.attacker.body.model.isNone(UnitModel.TROOP) )
    {
      params.attackPower -= 20;
    }
    else if ( params.battleRange > 1 )
    {
      params.attackPower += indirectBuff;
    }
  }

  private static class SnipeAttack extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Snipe Attack";
    private static final int COST = 3;
    private static final int VALUE = 20;
    Grit COcast;

    SnipeAttack(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Grit) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.indirectBuff += VALUE;
      COcast.addCOModifier(new IndirectRangeBoostModifier(1));
    }
  }

  private static class SuperSnipe extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Super Snipe";
    private static final int COST = 6;
    private static final int VALUE = 20;
    Grit COcast;

    SuperSnipe(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Grit) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.indirectBuff += VALUE;
      COcast.addCOModifier(new IndirectRangeBoostModifier(2));
    }
  }
}

