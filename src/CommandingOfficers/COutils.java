package CommandingOfficers;

import java.util.ArrayList;

import Terrain.MapMaster;
import Units.Unit;

public class COutils
{
  public static ArrayList<Unit> findMassDamageTargets(MapMaster map, Commander attacker)
  {
    ArrayList<Unit> victims = new ArrayList<>();

    for( int y = 0; y < map.mapHeight; y++ )
    {
      for( int x = 0; x < map.mapWidth; x++ )
      {
        Unit resi = map.getResident(x, y);
        if( resi != null && attacker.isEnemy(resi.CO) )
          victims.add(resi);
      }
    }
    return victims;
  }
}
