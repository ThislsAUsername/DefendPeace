package Engine.GameEvents;

import CommandingOfficers.CommanderAbility;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderAbilityEvent implements GameEvent
{
  private final CommanderAbility myAbility;
  GameEventQueue gameEvents;

  public CommanderAbilityEvent(CommanderAbility ability)
  {
    myAbility = ability;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    // TODO: CO Ability intro splash
    GameAnimation anim = null;
    if( !gameEvents.isEmpty() )
    {
      // Just grab the first one for now; TODO create compound animations.
      anim = gameEvents.peek().getEventAnimation(mapView);
    }
    return anim;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    for(GameEvent ge : gameEvents )
    {
      ge.sendToListener(listener);
    }
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    myAbility.activate(gameMap);
  }

  /** Called by AbilityAction before `getEventAnimation`, `performEvent`, or `sendToListener`. */
  public void generateAbilityEvents(MapMaster gameMap)
  {
    gameEvents = myAbility.getEvents(gameMap);
  }

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
