package Engine.GameEvents;

import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.Weapons.Weapon;

public class ResupplyEvent implements GameEvent
{
  private Unit target;

  public ResupplyEvent(Unit aTarget)
  {
    target = aTarget;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    // Top up fuel.
    target.fuel = target.model.maxFuel;

    // Add ammunition.
    for( Weapon gun : target.weapons )
    {
      gun.reload();
    }
  }
}
