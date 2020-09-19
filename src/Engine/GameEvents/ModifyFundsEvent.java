package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class ModifyFundsEvent implements GameEvent
{
  private final Commander beneficiary;
  private final int value;

  public ModifyFundsEvent(Commander beneficiary, int value)
  {
    this.beneficiary = beneficiary;
    this.value = value;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    // TODO
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    // TODO
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
