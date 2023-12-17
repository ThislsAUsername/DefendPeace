package Engine.StateTrackers;

import Engine.GameInstance;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Units.Unit;

/**
 * Tracks a simple kill count for each unit; joined units use the higher value.
 */
public class KillCountsTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  // I wrote a generic handler for this and I'm going to use it
  private CountManager<GameInstance, Unit> killCounts = new CountManager<>();

  public int getCountFor(Unit unit)
  {
    return killCounts.getCountFor(this.game, unit);
  }

  // Track

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary summary)
  {
    if( summary.attacker.after.getHealth() < 1 )
      killCounts.incrementCount(this.game, summary.defender.unit);

    if( summary.defender.after.getHealth() < 1 )
      killCounts.incrementCount(this.game, summary.attacker.unit);

    return null;
  }

  // Clean up

  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    int higherKills = Math.max(getCountFor(join.unitRecipient), getCountFor(join.unitDonor));
    killCounts.resetCountFor(this.game, join.unitDonor);
    killCounts.setCountFor(this.game, join.unitRecipient, higherKills);

    return null;
  }
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer healthBeforeDeath)
  {
    killCounts.resetCountFor(this.game, victim);

    return null;
  }
}
