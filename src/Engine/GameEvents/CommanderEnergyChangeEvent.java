package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;

public class CommanderEnergyChangeEvent implements GameEvent
{
  private final Commander beneficiary;
  private final double delta;
  private double deltaActual;

  public CommanderEnergyChangeEvent(Commander target, double change)
  {
    beneficiary = target;
    delta = change;
    deltaActual = 0;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return listener.receiveModifyCommanderEnergyEvent(beneficiary, deltaActual);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    double before = beneficiary.getAbilityPower();
    beneficiary.modifyAbilityPower(delta);

    deltaActual = beneficiary.getAbilityPower() - before;
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
