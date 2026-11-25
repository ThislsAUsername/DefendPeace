package CommandingOfficers.Variants.BH;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.BattleSummary;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import UI.UIUtils;
import Units.Unit;
import lombok.var;

public class VonBoltHP extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Von Bolt", UIUtils.SourceGames.VARIANTS, UIUtils.BH, "HP");
      infoPages.add(new InfoPage(
            "Von Bolt (HP drain)\n"
          + "Units gain +20% attack and +20% defense.\n"
          + "Units lose 1 HP after combat they initiate.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new VonBoltHP(rules);
    }
  }

  public VonBoltHP(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary summary)
  {
    GameEventQueue returnEvents = new GameEventQueue();

    if( this == summary.attacker.unit.CO )
    {
      var victim = new ArrayList<Unit>();
      victim.add(summary.attacker.unit);
      returnEvents.add(new MassDamageEvent(this, victim, 10, false));
    }

    return returnEvents;
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.attackPower += 20;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    params.defenseSubtraction += 20;
  }

}
