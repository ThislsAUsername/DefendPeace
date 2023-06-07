package Engine.GameEvents;

import CommandingOfficers.CommanderAbility;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderAbilityRevertEvent implements GameEvent
{
  private final CommanderAbility myAbility;

  public CommanderAbilityRevertEvent(CommanderAbility ability)
  {
    myAbility = ability;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveCommanderAbilityRevertEvent(myAbility);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    myAbility.deactivate(gameMap);
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
