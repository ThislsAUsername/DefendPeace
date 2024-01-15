package CommandingOfficers;

import java.util.ArrayList;

import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.MassDamageEvent;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class MeteorParams
{
  public XYCoord target; // This is allowed to be null. If it is, then this class will produce a no-op MassDamageEvent.
  public int power = 3; // Silo
  public int radius = 2; // ditto
  public boolean selfHarm = true;
  public boolean inflictStun = false;
  public MeteorParams(XYCoord targetPosition, int radius)
  {
    target = targetPosition;
    this.radius = radius;
  }

  public MassDamageEvent getDamage(MapMaster map, Commander attacker)
  {
    if( null == target )
      return new MassDamageEvent(attacker, new ArrayList<>(), power*10, false, inflictStun);

    ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, target, 0, radius);
    ArrayList<Unit> victimList = new ArrayList<>();
    for( XYCoord loc : locations )
    {
      Unit victim = map.getResident(loc);
      if( victim != null && // Something is there.
          (selfHarm || victim.CO.isEnemy(attacker)) ) // It's not friendly enough to spare.
      {
        victimList.add(victim);
      }
    }
    return new MassDamageEvent(attacker, victimList, power*10, false, inflictStun);
  }

  private static int aggregateTargetScore(GameMap map, Commander attacker, XYCoord target, int radius, IValueFinder evaluator)
  {
    int currentValue = 0;

    ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, target, 0, radius);
    for( XYCoord loc : locations )
    {
      Unit found = map.getResident(loc);
      if( found != null ) // Something is there.
      {
        currentValue += evaluator.getValue(attacker, found);
      }
    }

    return currentValue;
  }

  /**
   * @return A MeteorParams with the best target, or no target if there are no values above 0.
   */
  public static MeteorParams planMeteor(GameMap map, Commander attacker, int radius, IValueFinder evaluator)
  {
    XYCoord maxTarget = null;
    int maxValue = 0;
    for( int i = 0; i < map.mapWidth; i++ )
    {
      for( int j = 0; j < map.mapHeight; j++ )
      {
        XYCoord currentTarget = new XYCoord(i, j);
        int currentValue = aggregateTargetScore(map, attacker, currentTarget, radius, evaluator);
        if( currentValue > maxValue )
        {
          maxValue = currentValue;
          maxTarget = currentTarget;
        }
      }
    }
    return new MeteorParams(maxTarget, radius);
  }

  /**
   * Restricts targeting to always center on enemy units.
   * @param targetingMap The map to use to find units to center on.
   * @param scoringMap The map to use to score valid strike locations.
   * @return A MassStrikeParams with the best target, or no target if there are no values above 0.
   */
  public static MeteorParams planMeteorOnEnemy(GameMap targetingMap, GameMap scoringMap, Commander attacker, int radius, IValueFinder evaluator)
  {
    XYCoord maxTarget = null;
    int maxValue = 0;
    for( int i = 0; i < targetingMap.mapWidth; i++ )
    {
      for( int j = 0; j < targetingMap.mapHeight; j++ )
      {
        Unit victim = targetingMap.getResident(i, j);
        if( victim == null || !victim.CO.isEnemy(attacker) )
          continue;

        XYCoord currentTarget = new XYCoord(i, j);
        int currentValue = aggregateTargetScore(scoringMap, attacker, currentTarget, radius, evaluator);
        if( currentValue > maxValue )
        {
          maxValue = currentValue;
          maxTarget = currentTarget;
        }
      }
    }
    return new MeteorParams(maxTarget, radius);
  }
}
