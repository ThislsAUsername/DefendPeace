package CommandingOfficers.BlueMoon;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class Grit extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Grit");
      infoPages.add(new InfoPage(
          "Grit\r\n" + 
          "  Indirect units have +1 range and gain +20% attack. Direct units lose -20% attack (footsoldiers are normal)\r\n" + 
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

    for( UnitModel um : unitModels.values() )
    {
      for( WeaponModel pewpew : um.weaponModels )
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
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    Unit minion = null;
    if( params.attacker.CO == this )
    {
      minion = params.attacker;
    }

    if( null != minion )
    {
      if( params.combatRef.battleRange == 1 && minion.model.chassis != ChassisEnum.TROOP )
      {
        params.attackFactor -= 20;
      }
      else if ( params.combatRef.battleRange > 1 )
      {
        params.attackFactor += indirectBuff;
      }
    }
  }

  private static class SnipeAttack extends CommanderAbility
  {
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

