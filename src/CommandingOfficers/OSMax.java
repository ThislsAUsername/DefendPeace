package CommandingOfficers;

import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.WeaponModel;

public class OSMax extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Max", new instantiator());
  private static class instantiator implements COMaker
  {
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

    for( UnitModel um : unitModels )
    {
      if( um.weaponModels != null )
      {
        boolean debuff = false;
        for( WeaponModel pewpew : um.weaponModels )
        {
          if( !pewpew.canFireAfterMoving )
          {
            pewpew.maxRange -= 1;
            debuff = true;
          }
        }
        if (debuff)
          um.modifyDamageRatio(-10);
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
  public void initTurn(GameMap map)
  {
    this.directBuff = 20;
    super.initTurn(map);
  }

  public void applyCombatModifiers(BattleParams params, GameMap map)
  {
    Unit minion = null;
    if( params.attacker.CO == this )
    {
      minion = params.attacker;
    }

    if( null != minion )
    {
      if( params.combatRef.battleRange == 1 )
      {
        params.attackFactor += directBuff;
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
    protected void perform(GameMap gameMap)
    {
      COcast.directBuff += VALUE;
      COMovementModifier moveMod = new COMovementModifier(1);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.chassis != ChassisEnum.TROOP && um.getDamageRatio() > 100)
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
    protected void perform(GameMap gameMap)
    {
      COcast.directBuff += VALUE;

      COMovementModifier moveMod = new COMovementModifier(2);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.chassis != ChassisEnum.TROOP && um.getDamageRatio() > 100)
          moveMod.addApplicableUnitModel(um);
      }

      myCommander.addCOModifier(moveMod);
    }
  }
}
