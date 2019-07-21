package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class OSMax extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Max");
      infoPages.add(new InfoPage(
          "Max\r\n" + 
          "  Direct units gain +20% attack. Indirect units lose -10% attack, and have -1 range\r\n" + 
          "Max Force -- Direct units gain +1 Movement and their power increases by +10%\r\n" + 
          "Max Blast -- Direct Units gain +2 Movement and their power increases by +30%"));
    }
    @Override
    public Commander create()
    {
      return new OSMax();
    }
  }
  
  public int directBuff = 20;

  public OSMax()
  {
    super(coInfo);

    for( UnitModel um : unitModels.values() )
    {
      if( um.weaponModels != null )
      {
        for( WeaponModel pewpew : um.weaponModels )
        {
          if( pewpew.maxRange > 1 )
          {
            pewpew.maxRange -= 1;
          }
        }
      }
    }

    addCommanderAbility(new MaxForce(this));
    addCommanderAbility(new MaxBlast(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    this.directBuff = 20;
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
        params.attackFactor += directBuff;
      }
      else if ( params.combatRef.battleRange > 1 )
      {
        params.attackFactor -= 10;
      }
    }
  }

  private static class MaxForce extends CommanderAbility
  {
    private static final String NAME = "Max Force";
    private static final int COST = 3;
    private static final int VALUE = 10;
    OSMax COcast;

    MaxForce(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (OSMax) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.directBuff += VALUE;
      COMovementModifier moveMod = new COMovementModifier(1);

      for( UnitModel um : COcast.unitModels.values() )
      {
        if( um.chassis != ChassisEnum.TROOP && um.hasDirectFireWeapon() )
          moveMod.addApplicableUnitModel(um);
      }

      myCommander.addCOModifier(moveMod);
    }
  }

  private static class MaxBlast extends CommanderAbility
  {
    private static final String NAME = "Max Blast";
    private static final int COST = 6;
    private static final int VALUE = 30;
    OSMax COcast;

    MaxBlast(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (OSMax) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.directBuff += VALUE;

      COMovementModifier moveMod = new COMovementModifier(2);

      for( UnitModel um : COcast.unitModels.values() )
      {
        if( um.chassis != ChassisEnum.TROOP && um.hasDirectFireWeapon() )
          moveMod.addApplicableUnitModel(um);
      }

      myCommander.addCOModifier(moveMod);
    }
  }
}

