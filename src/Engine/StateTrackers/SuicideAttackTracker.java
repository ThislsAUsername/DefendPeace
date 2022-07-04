package Engine.StateTrackers;

import java.util.HashSet;

import Engine.Utils;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import Units.UnitModel;

/**
 * Kills off the registered unit type after it attacks
 */
public class SuicideAttackTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public HashSet<UnitModel> killTypeMap = new HashSet<>();

  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary summary)
  {
    if(!killTypeMap.contains(summary.attacker.model))
      return null;

    GameEventQueue events = new GameEventQueue();
    final boolean canLose = true;
    Utils.enqueueDeathEvent(summary.attacker.unit, summary.attacker.after.coord, canLose, events);

    return events;
  }
}
