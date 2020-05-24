package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.WeaponModel;

public class Max extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Max");
      infoPages.add(new InfoPage(
          "Max\r\n" +
          "  Indirects lose -1 range.\r\n" +
          "  +20% firepower in non-footsoldier direct combat.\r\n" +
          "  -10% firepower in indirect combat\r\n" +
          "Max Force -- Direct units gain +1 Movement and their power increases by +10%\r\n" +
          "Max Blast -- Direct Units gain +2 Movement and their power increases by +30%"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Max(rules);
    }
  }
  
  public int directBuff = 20;

  public Max(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.weapons != null )
      {
        for( WeaponModel pewpew : um.weapons )
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
  public GameEventQueue initTurn(MapMaster map)
  {
    this.directBuff = 20;
    return super.initTurn(map);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.battleRange == 1 && params.attacker.body.model.isNone(UnitModel.TROOP) )
    {
      params.attackPower += directBuff;
    }
    else if ( params.battleRange > 1 )
    {
      params.attackPower -= 10;
    }
  }

  private static class MaxForce extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Max Force";
    private static final int COST = 3;
    private static final int VALUE = 10;
    Max COcast;

    MaxForce(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Max) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.directBuff += VALUE;
      COMovementModifier moveMod = new COMovementModifier(1);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.isNone(UnitModel.TROOP) && um.hasDirectFireWeapon() )
          moveMod.addApplicableUnitModel(um);
      }

      myCommander.addCOModifier(moveMod);
    }
  }

  private static class MaxBlast extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Max Blast";
    private static final int COST = 6;
    private static final int VALUE = 30;
    Max COcast;

    MaxBlast(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Max) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.directBuff += VALUE;

      COMovementModifier moveMod = new COMovementModifier(2);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.isNone(UnitModel.TROOP) && um.hasDirectFireWeapon() )
          moveMod.addApplicableUnitModel(um);
      }

      myCommander.addCOModifier(moveMod);
    }
  }
}

