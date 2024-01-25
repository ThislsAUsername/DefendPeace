package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

/**
 * Does all the tag-type-specific swapping of ownership of things
 */
public class SwapCOEvent extends TurnEndEvent
{
  Commander swapTarget;

  public SwapCOEvent(Army army, int turnNum, Commander swapTarget)
  {
    super(army, turnNum);
    this.swapTarget = swapTarget;
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    int swapIndex = 0;
    for( int i = 0; i < army.cos.length; ++i )
    {
      if( swapTarget == army.cos[i] )
      {
        swapIndex = i;
        break;
      }
    }

    Commander temp = army.cos[0];
    army.cos[0] = army.cos[swapIndex];
    army.cos[swapIndex] = temp;

    switch (army.gameRules.tagMode)
    {
      case AWBW:
        for( Unit u : army.getUnits() )
        {
          if( u.CO == swapTarget )
            continue;
          u.CO.units.remove(u);
          u.CO = swapTarget;
          swapTarget.units.add(u);
        }
        shiftPropsTo(gameMap, army, swapTarget);
        break;
      case Persistent:
        shiftPropsTo(gameMap, army, swapTarget);
        break;
      case OFF:
      case Team_Merge:
        break;
    }
  }

  public static void shiftPropsTo(MapMaster gameMap, Army army, Commander swapTarget)
  {
    for( XYCoord xyc : army.getOwnedProperties() )
    {
      gameMap.setOwner(swapTarget, xyc);
    }
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    // TODO: Swoosh
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    return null;
  }

  @Override
  public boolean shouldEndTurn()
  {
    return false;
  }
}
