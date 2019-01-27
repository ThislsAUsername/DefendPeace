package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.BattleSummary;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Engine.Combat.BattleInstance.BattleParams;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class IDSTabithaBasic extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Tabitha\nBasic", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new IDSTabithaBasic();
    }
  }

  public IDSTabithaBasic()
  {
    super(coInfo);

    addCommanderAbility(new Apocolypse(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  
  private Unit COU;

  @Override
  public void initTurn(GameMap map)
  {
    this.COU = null;
    super.initTurn(map);
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( params.attacker.CO == this )
    {
      Unit minion = params.attacker;

      if( null == COU || minion == COU )
        params.attackFactor += 35;
    }

    if( params.defender.CO == this )
    {
      Unit minion = params.defender;

      if( null == COU || minion == COU )
        params.defenseFactor += 35;
    }

  }

  @Override
  public void receiveBattleEvent(BattleSummary battleInfo)
  {
    super.receiveBattleEvent(battleInfo);
    // Determine if we were part of this fight.
    if( COU == null && battleInfo.attacker.CO == this )
    {
      COU = battleInfo.attacker;
    }
  }

  private static class Apocolypse extends CommanderAbility
  {
    private static final String NAME = "Firestorm";
    private static final int COST = 10;
    private static final int POWER = 8;
    IDSTabithaBasic COcast;

    Apocolypse(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (IDSTabithaBasic) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // make our COU an enemy unit so we can't stack buffs
      for( Commander co : gameMap.commanders )
      {
        if (myCommander.isEnemy(co))
        {
          for (Unit lol : co.units)
          {
            COcast.COU = lol;
            break;
          }
        }
      }
      myCommander.addCOModifier(new CODamageModifier(25));
      myCommander.addCOModifier(new CODefenseModifier(25));
      MassStrikeUtils.damageStrike(gameMap, POWER,
          MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true)), 2);
    }
  }
}
