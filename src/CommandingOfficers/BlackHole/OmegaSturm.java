package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.PerfectMoveModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;

public class OmegaSturm extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Omega Sturm");
      infoPages.add(new InfoPage(
          "Called \"Omega\" because he's extra fair and balanced.\n" +
          "  Terrain cost is 1 on all terrain. Units gain +20% attack and defense\r\n" +
          "  Gains "+CHARGERATIO_FUNDS+" funds of SCOP charge every turn\n" + 
          "Meteor Strike (10):\n" +
          "Just like you remember it ;)"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new OmegaSturm(rules);
    }
  }

  public OmegaSturm(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    new PerfectMoveModifier(false).applyChanges(this);
    new CODamageModifier (20).applyChanges(this);
    new CODefenseModifier(20).applyChanges(this);

    addCommanderAbility(new MeteorStrike(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    modifyAbilityPower(1);
    return super.initTurn(map);
  }

  private static class MeteorStrike extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Meteor Strike";
    private static final int COST = 13;
    private static final int POWER = 8;

    MeteorStrike(Commander commander)
    {
      super(commander, NAME, COST);
      AIFlags |= PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(new CODamageModifier(10));
      myCommander.addCOModifier(new CODefenseModifier(20));
      MassStrikeUtils.damageStrike(gameMap, POWER,
          MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true)), 2);
    }
  }
}

