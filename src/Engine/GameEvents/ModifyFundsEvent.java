package Engine.GameEvents;

import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class ModifyFundsEvent implements GameEvent
{
  private final Army beneficiary;
  private final int value;

  public ModifyFundsEvent(Army beneficiary, int value)
  {
    this.beneficiary = beneficiary;
    this.value = value;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    // TODO: Create listener hook
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveModifyFundsEvent(beneficiary, value);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    beneficiary.money += value;
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
