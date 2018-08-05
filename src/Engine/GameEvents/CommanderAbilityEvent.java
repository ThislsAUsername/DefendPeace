package Engine.GameEvents;

import CommandingOfficers.CommanderAbility;
import Terrain.GameMap;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderAbilityEvent implements GameEvent
{
  private final CommanderAbility myAbility;

  public CommanderAbilityEvent(CommanderAbility ability)
  {
    myAbility = ability;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void performEvent(GameMap gameMap)
  {
    myAbility.activate(gameMap);
  }
}
