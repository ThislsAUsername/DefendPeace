package Engine.GameEvents;

import CommandingOfficers.Commander;
import Terrain.Environment;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.Environment.Terrains;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderDefeatEvent implements GameEvent
{
  private final Commander defeatedCO;

  public CommanderDefeatEvent( Commander co )
  {
    defeatedCO = co;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildCommanderDefeatAnimation( this );
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    // Set the flag so that we know he's toast.
    defeatedCO.isDefeated = true;

    // Loop through the map and clean up any of the defeated CO's assets.
    for(int y = 0; y < gameMap.mapHeight; ++y)
    {
      for(int x = 0; x < gameMap.mapWidth; ++x)
      {
        Location loc = gameMap.getLocation(x, y);

        // Remove any units that remain.
        if(loc.getResident() != null && loc.getResident().CO == defeatedCO)
        {
          loc.setResident(null);
        }
        defeatedCO.units.clear(); // Remove from the CO array too, just to be thorough.

        // Downgrade the defeated commander's HQ to a city.
        defeatedCO.HQLocation.setEnvironment(Environment.getTile(Terrains.CITY, defeatedCO.HQLocation.getEnvironment().weatherType));

        // Release control of any buildings he owned.
        if(loc.isCaptureable() && loc.getOwner() == defeatedCO)
        {
          loc.setOwner(null);
        }
      } // ~width loop
    } // ~height loop
  } // ~performEvent
}
