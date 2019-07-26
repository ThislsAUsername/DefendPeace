package CommandingOfficers.YellowComet;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class Spannbei extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Spannbei");
      infoPages.add(new InfoPage(
          "   Spannbei\r\n" + 
          "  Units cost +50% more to build, but gain +30% attack and defense\r\n" + 
          "Morale Boost -- Unit attack is increased by +10%\r\n" + 
          "Samurai Spirit -- Boosts attack by 10%, defense by 20%, and his counterattacks do 50% more damage"));
    }
    @Override
    public Commander create()
    {
      return new Spannbei();
    }
  }

  private double counterMult = 1;

  public Spannbei()
  {
    super(coInfo);

    new CODamageModifier(30).applyChanges(this);
    new CODefenseModifier(30).applyChanges(this);
    for( UnitModel um : unitModels.values() )
    {
      um.COcost = 1.5;
    }

    addCommanderAbility(new MoraleBoost(this));
    addCommanderAbility(new SamuraiSpirit(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    counterMult = 1;
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

    if( null != minion && params.isCounter )
    {
      // it's a multiplier according to the damage calc
      params.attackFactor *= counterMult;
    }
  }

  private static class MoraleBoost extends CommanderAbility
  {
    private static final String NAME = "Morale Boost";
    private static final int COST = 3;
    private static final int VALUE = 10;

    MoraleBoost(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(VALUE));
    }
  }

  private static class SamuraiSpirit extends CommanderAbility
  {
    private static final String NAME = "Samurai Spirit";
    private static final int COST = 6;
    private Spannbei COcast;

    SamuraiSpirit(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Spannbei) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(10));
      myCommander.addCOModifier(new CODefenseModifier(20));
      COcast.counterMult = 1.5;
    }
  }
}

