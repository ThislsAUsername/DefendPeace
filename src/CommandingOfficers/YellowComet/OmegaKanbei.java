package CommandingOfficers.YellowComet;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.BattleInstance.BattleParams;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class OmegaKanbei extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Omega Kanbei");
      infoPages.add(new InfoPage(
          "Called \"Omega\" because he's extra fair and balanced.\n" +
          "  Units cost +40% more to build, but gain +50% attack and defense\r\n" +
          "  Deals 1.5x damage on counterattacks\r\n" +
          "Lightning Samurai (13): All units are refreshed"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new OmegaKanbei(rules);
    }
  }

  private double counterMult = 1.5;

  public OmegaKanbei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    new CODamageModifier(50).applyChanges(this);
    new CODefenseModifier(50).applyChanges(this);
    for( UnitModel um : unitModels.values() )
    {
      um.COcost = 1.4;
    }

    addCommanderAbility(new LightningSamurai(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
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

  private static class LightningSamurai extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lightning Samurai";
    private static final int COST = 13;

    LightningSamurai(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit unit : myCommander.units )
      {
        unit.isTurnOver = false;
      }
    }
  }
}

