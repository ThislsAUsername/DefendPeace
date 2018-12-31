package Engine.GameEvents;

import CommandingOfficers.Commander;
import Terrain.Environment;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderDefeatEvent implements GameEvent
{
  private final Commander defeatedCO;
  private Commander beneficiaryCO = null;

  public CommanderDefeatEvent( Commander co )
  {
    defeatedCO = co;
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
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveCommanderDefeatEvent( this );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    // Set the flag so that we know he's toast.
    defeatedCO.isDefeated = true;

    // Loop through the CO's units and remove each from the map.
    for( Units.Unit unit : defeatedCO.units )
    {
      gameMap.removeUnit( unit );
    }

    // Clear the CO array too, just to be thorough.
    defeatedCO.units.clear();

    // Downgrade the defeated commander's HQ to a city, unless they don't have a proper HQ.
    Location HQLoc = gameMap.getLocation(defeatedCO.HQLocation);
    if( HQLoc.getEnvironment().terrainType == TerrainType.HEADQUARTERS )
    {
      HQLoc.setEnvironment(Environment.getTile(TerrainType.CITY, HQLoc.getEnvironment().weatherType));
    }

    // Loop through the map and revoke all of the CO's properties.
    for(int y = 0; y < gameMap.mapHeight; ++y)
    {
      for(int x = 0; x < gameMap.mapWidth; ++x)
      {
        Location loc = gameMap.getLocation(x, y);

        // Release control of any buildings he owned.
        if(loc.isCaptureable() && loc.getOwner() == defeatedCO)
        {
          loc.setOwner(beneficiaryCO);
        }
      } // ~width loop
    } // ~height loop
  } // ~performEvent
}
