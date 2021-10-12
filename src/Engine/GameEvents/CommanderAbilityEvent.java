package Engine.GameEvents;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderAbilityEvent implements GameEvent
{
  private final Commander myCommander;
  private final CommanderAbility myAbility;

  public CommanderAbilityEvent(Commander co, CommanderAbility ability)
  {
    myCommander = co;
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
    // TODO: Create listener hook
    return null;
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( myCommander.getAbilityPower() < myAbility.getCost() )
    {
      System.out.println("WARNING!: Performing ability with insufficient ability power!");
    }
    myCommander.activateAbility(myAbility, gameMap);
    myAbility.adjustCost();

    myAbility.activate(myCommander, gameMap);
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
