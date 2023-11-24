package Engine.GameEvents;

import CommandingOfficers.CommanderAbility;
import Engine.XYCoord;
import Terrain.MapMaster;
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
    // TODO: CO Ability intro splash
    GameAnimation anim = null;
    return anim;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveCommanderAbilityEvent(myAbility);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    myAbility.activate(gameMap);
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
