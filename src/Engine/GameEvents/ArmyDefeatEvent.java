package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.XYCoord;
import Terrain.Environment;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class ArmyDefeatEvent implements GameEvent
{
  private final Army defeatedArmy;
  private Commander beneficiaryCO = null;

  public ArmyDefeatEvent( Army co )
  {
    defeatedArmy = co;
  }

  public void setPropertyBeneficiary( Commander beneficiary )
  {
    beneficiaryCO = beneficiary;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildCommanderDefeatAnimation( this );
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveCommanderDefeatEvent( this );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    // Set the flag so that we know he's toast.
    defeatedArmy.isDefeated = true;

    // Loop through the CO's units and remove each from the map.
    for( Units.Unit unit : defeatedArmy.getUnits() )
    {
      gameMap.removeUnit( unit );
    }

    // Downgrade the defeated army's HQ to a city, unless they don't have a proper HQ.
    for(XYCoord hqCoord : defeatedArmy.HQLocations)
    {
      MapLocation HQLoc = gameMap.getLocation(hqCoord);
      if( HQLoc.getEnvironment().terrainType == TerrainType.HEADQUARTERS )
      {
        HQLoc.setEnvironment(Environment.getTile(TerrainType.CITY, HQLoc.getEnvironment().weatherType));
      }
    }

    // Loop through the map and revoke all of the CO's properties.
    for(int y = 0; y < gameMap.mapHeight; ++y)
    {
      for(int x = 0; x < gameMap.mapWidth; ++x)
      {
        MapLocation loc = gameMap.getLocation(x, y);
        Commander owner = loc.getOwner();

        // Release control of any buildings he owned.
        if(loc.isCaptureable() && null != owner && owner.army == defeatedArmy)
        {
          gameMap.setOwner(beneficiaryCO, x, y);
          Unit resident = loc.getResident();
          if( null != resident )
            resident.stopCapturing();
        }
      } // ~width loop
    } // ~height loop
  } // ~performEvent

  @Override
  public XYCoord getStartPoint()
  {
    return null;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return null;
  }
}
