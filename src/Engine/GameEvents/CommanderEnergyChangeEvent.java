package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderEnergyChangeEvent implements GameEvent
{
  private final Commander beneficiary;
  private final int deltaStars;
  private int deltaActualFunds;

  public CommanderEnergyChangeEvent(Commander target, int stars)
  {
    beneficiary = target;
    deltaStars = stars;
    deltaActualFunds = 0;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveModifyCommanderEnergyEvent(beneficiary, deltaActualFunds);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    int before = beneficiary.getAbilityPower();
    beneficiary.modifyAbilityStars(deltaStars);

    deltaActualFunds = beneficiary.getAbilityPower() - before;
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
