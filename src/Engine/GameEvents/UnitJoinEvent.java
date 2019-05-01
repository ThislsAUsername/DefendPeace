package Engine.GameEvents;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public class UnitJoinEvent implements GameEvent
{
  private Unit unitDonor = null;
  private Unit unitRecipient = null;

  public UnitJoinEvent( Unit donor, Unit recipient )
  {
    unitDonor = donor;
    unitRecipient = recipient;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return mapView.buildUnitJoinAnimation();
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveUnitJoinEvent( this );
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( null != unitRecipient && (unitRecipient.getHP() < unitRecipient.model.maxHP) )
    {
      // Crunch the numbers we need up front.
      int donorHP = unitDonor.getHP();
      int neededHP = unitRecipient.model.maxHP - unitRecipient.getHP();
      int extraHP = donorHP - neededHP;
      if( extraHP < 0 ) extraHP = 0;

      // Actually add the HP.
      unitRecipient.alterHP(donorHP);

      // If we had extra HP, add that as income.
      double costPerHP = unitDonor.model.getCost()/unitDonor.model.maxHP;
      unitDonor.CO.money += (extraHP * costPerHP);

      // Remove the donor unit.
      gameMap.removeUnit(unitDonor);
    }
    else
    {
      System.out.println("WARNING! Cannot join " + unitDonor.model.type + " with " + unitRecipient.model.type );
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(unitDonor.x, unitDonor.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(unitRecipient.x, unitRecipient.y);
  }
}
