package CommandingOfficers;

import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class BMGrit extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Grit", new instantiator());
  private static class instantiator extends COMaker
  {
    public instantiator()
    {
      infoPages.add(new InfoPage(
          "Grit\r\n" + 
          "  Indirect units have +1 range and gain +20% attack. Direct units lose -20% attack (footsoldiers are normal)\r\n" + 
          "Snipe Attack -- Indirect units gain +1 Range and +20% attack\r\n" + 
          "Super Snipe -- Indirect units gain +2 Range and +20% attack"));
    }
    @Override
    public Commander create()
    {
      return new BMGrit();
    }
  }
  
  public int indirectBuff = 20;

  public BMGrit()
  {
    super(coInfo);

    for( UnitModel um : unitModels )
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
  public GameEventQueue initTurn(GameMap map)
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
    BMGrit COcast;

    SnipeAttack(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BMGrit) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.indirectBuff += VALUE;
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(COcast, 1);
      COcast.addCOModifier(rangeBoost);
    }
  }

  private static class SuperSnipe extends CommanderAbility
  {
    private static final String NAME = "Super Snipe";
    private static final int COST = 6;
    private static final int VALUE = 20;
    BMGrit COcast;

    SuperSnipe(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BMGrit) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.indirectBuff += VALUE;
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(COcast, 2);
      COcast.addCOModifier(rangeBoost);
    }
  }
}
