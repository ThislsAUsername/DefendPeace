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

    // Loop through the CO's units and remove each from the map.
    for( Units.Unit unit : defeatedCO.units )
    {
      gameMap.removeUnit( unit );
    }

    // Clear the CO array too, just to be thorough.
    defeatedCO.units.clear();

    // Downgrade the defeated commander's HQ to a city.
    defeatedCO.HQLocation.setEnvironment(Environment.getTile(Terrains.CITY, defeatedCO.HQLocation.getEnvironment().weatherType));

    // Loop through the map and revoke all of the CO's properties.
    for(int y = 0; y < gameMap.mapHeight; ++y)
    {
      for(int x = 0; x < gameMap.mapWidth; ++x)
      {
        Location loc = gameMap.getLocation(x, y);

        // Release control of any buildings he owned.
        if(loc.isCaptureable() && loc.getOwner() == defeatedCO)
        {
          loc.setOwner(null);
        }
      } // ~width loop
    } // ~height loop
  } // ~performEvent
}
