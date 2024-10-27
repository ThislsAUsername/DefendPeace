package Engine.StateTrackers;

import java.awt.Color;
import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GamePath;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Units.Unit;

public class LightningUnitTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  private CountManager<Commander, Unit> extraActionCounts = new CountManager<>();
  
  private int getCountFor(Unit unit)
  {
    return extraActionCounts.getCountFor(unit.CO, unit);
  }
  private void setCountFor(Unit unit, int actions)
  {
    extraActionCounts.setCountFor(unit.CO, unit, actions);
  }
  /**
   * Add one action to the unit's available total this turn.
   */
  public void giveAction(Unit unit)
  {
    if( unit.isStunned )
      unit.isStunned = false;
    else if( unit.isTurnOver )
      unit.isTurnOver = false;
    else
      extraActionCounts.incrementCount(unit.CO, unit);
  }
  public void resetFor(Commander co)
  {
    extraActionCounts.resetCountFor(co);
  }

  // Mark units I will double-move
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    if( getCountFor(unit) > 0 )
      return 'S';
    return '\0';
  }
  public Color getMarkingColor(Unit unit)
  {
    Integer extraActions = getCountFor(unit);
    if( extraActions < 1 )
      return Color.white;

    Color mc = unit.CO.myColor.darker();
    while (extraActions > 1) // Make extra actions more distinct
    {
      --extraActions;
      mc = mc.brighter();
    }
    return mc;
  }

  // Handle actually doling out extra turns.

  public GameEventQueue receiveMoveEvent(Unit unit, GamePath unitPath)
  {
    Integer extraActions = getCountFor(unit);

    if( extraActions < 1 )
      return null;
    setCountFor(unit, extraActions-1);
    unit.isTurnOver = false;

    return null;
  }
  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    // The donor unit doesn't really exist anymore, so we can only do things to the recipient 
    Unit donor = join.unitDonor;
    Unit reactivatable = join.unitRecipient;

    Integer donorActions = getCountFor(donor);
    if( !join.unitDonor.isTurnOver ) // We already gave this dude his turn back after moving
      ++donorActions;
    extraActionCounts.resetCountFor(donor.CO, donor);

    final int totalExtraActions = getCountFor(reactivatable) + donorActions;

    if( totalExtraActions < 1 )
      return null;

    join.unitRecipient.isTurnOver = false;
    setCountFor(reactivatable, totalExtraActions-1);

    return null;
  }

  // Clean up
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer healthBeforeDeath)
  {
    extraActionCounts.resetCountFor(victim.CO, victim);

    return null;
  }
}
