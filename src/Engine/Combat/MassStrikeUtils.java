package Engine.Combat;

import java.util.ArrayList;
import CommandingOfficers.Commander;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.GameMap;
import Units.Unit;

public class MassStrikeUtils
{

  public static XYCoord findValueConcentration(GameMap map, int maxRange, IValueFinder evaluator)
  {
    XYCoord maxTarget = null;
    int maxValue = 0;
    for( int i = 0; i < map.mapWidth; i++ )
    {
      for( int j = 0; j < map.mapHeight; j++ )
      {
        XYCoord currentTarget = new XYCoord(i, j);
        int currentValue = 0;

        ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, currentTarget, 0, maxRange);
        for( XYCoord loc : locations )
        {
          Unit found = map.getLocation(loc).getResident();
          if( found != null ) // Something is there.
          {
            currentValue += evaluator.getValue(found);
          }
        }
        if( currentValue > maxValue )
        {
          maxValue = currentValue;
          maxTarget = currentTarget;
        }
      }
    }
    return maxTarget;
  }

  public static ArrayList<Unit> missileStrike(GameMap map, XYCoord targetPosition)
  {
    return damageStrike(map, 3, targetPosition, 2);
  }
  public static ArrayList<Unit> damageStrike(GameMap map, int power, XYCoord targetPosition, int maxRange)
  {
    return damageStrike(map, power, targetPosition, 0, maxRange, null, true);
  }
  public static ArrayList<Unit> damageStrike(GameMap map, int power, XYCoord targetPosition, int minRange, int maxRange,
      Commander co, boolean selfHarm)
  {
    ArrayList<XYCoord> locations = Utils.findLocationsInRange(map, targetPosition, minRange, maxRange);
    ArrayList<Unit> victimList = new ArrayList<>();
    for( XYCoord loc : locations )
    {
      Unit victim = map.getLocation(loc).getResident();
      if( victim != null && // Something is there.
          (selfHarm || victim.CO.isEnemy(co)) ) // It's not friendly enough to spare.
      {
        victim.alterHP(-power);
        victimList.add(victim);
      }
    }
    return victimList;
  }
}
