package CommandingOfficers.YellowComet;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Units.UnitModel;

public class Spannbei extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
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
    public Commander create(GameScenario.GameRules rules)
    {
      return new Spannbei(rules);
    }
  }

  private double counterMult = 1;

  public Spannbei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    new CODamageModifier(30).applyChanges(this);
    new CODefenseModifier(30).applyChanges(this);
    for( UnitModel um : unitModels )
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
  public GameEventQueue initTurn(MapMaster map)
  {
    counterMult = 1;
    return super.initTurn(map);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.isCounter )
    {
      // it's a multiplier according to the damage calc
      params.attackPower *= counterMult;
    }
  }

  private static class MoraleBoost extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
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
    private static final long serialVersionUID = 1L;
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

