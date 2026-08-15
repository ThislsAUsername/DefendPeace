package Engine.StateTrackers;

import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Terrain.MapLocation;
import Units.Unit;

public class SamiLevelTracker extends StateTracker
{
  private static final long serialVersionUID = 1L;

  public static enum SamiRank
  {
    LEVEL5('V', 5),
    LEVEL4('4', 4),
    LEVEL3('3', 3),
    LEVEL2('Ⅱ', 2),
    LEVEL1('I', 1),
    NONE('\0', 0);
    public final char mark;
    public final int  exp;
    private SamiRank(char mark, int exp)
    {
      this.mark = mark;
      this.exp  = exp;
    }
  }
  public SamiRank getRank(Unit unit)
  {
    int exp = getExperience(unit);
    for( SamiRank rank : SamiRank.values() )
      if( exp >= rank.exp )
        return rank;
    return SamiRank.NONE; // shouldn't be hit
  }

  public HashMap<Unit, Integer> experience = new HashMap<>();

  // EXP gain
  @Override
  public GameEventQueue receiveBattleEvent(BattleSummary summary)
  {
    if( summary.attacker.after.getHealth() < 1 )
      addExperience(summary.defender.unit, 1);

    if( summary.defender.after.getHealth() < 1 )
      addExperience(summary.attacker.unit, 1);

    return null;
  }
  @Override
  public GameEventQueue receiveCaptureEvent(Unit unit, Commander prevOwner, MapLocation location)
  {
    if( location.getOwner() == unit.CO )
      addExperience(unit, 1);
    return null;
  }

  // Other EXP management
  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent event)
  {
    int donorXP = 0;
    if( experience.containsKey(event.unitDonor) )
      donorXP = experience.remove(event.unitDonor);
    if( donorXP > getExperience(event.unitRecipient) )
      experience.put(event.unitRecipient, donorXP);
    return null;
  };
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
  {
    experience.remove(victim);
    return null;
  }

  public int getExperience(Unit profiteer)
  {
    if( experience.containsKey(profiteer) )
      return experience.get(profiteer);
    return addExperience(profiteer, 0);
  }
  public int addExperience(Unit profiteer, int profit)
  {
    if( !experience.containsKey(profiteer) )
    {
      experience.put(profiteer, 0);
    }
    int xp = experience.get(profiteer);
    int finalVal = xp + profit;
    if( finalVal > SamiRank.LEVEL5.exp )
      finalVal = SamiRank.LEVEL5.exp;
    experience.put(profiteer, finalVal);

    return finalVal;
  }

}
