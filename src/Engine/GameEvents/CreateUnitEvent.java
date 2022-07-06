package Engine.GameEvents;

import CommandingOfficers.Commander;
import Engine.Utils;
import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

public class CreateUnitEvent implements GameEvent
{
  private final Commander myCommander;
  private final Unit myNewUnit;
  private final XYCoord myBuildCoords;
  private int myFudgeRadius = 0;
  private final AnimationStyle myAnimStyle;
  private final boolean myBoots;

  public enum AnimationStyle
  {
    NONE,
    DROP_IN
  }

  public CreateUnitEvent(Commander commander, UnitModel model, XYCoord coords)
  {
    this(commander, model, coords, AnimationStyle.NONE, false, false);
  }
  /**
   * Allows spawning the unit on a tile within a radius, in case the "correct" tile is blocked
   */
  public CreateUnitEvent(Commander commander, UnitModel model, XYCoord coords, AnimationStyle anim, boolean unitIsReady, int fudgeRadius)
  {
    this(commander, model, coords, anim, unitIsReady, false);
    myFudgeRadius = fudgeRadius;
  }
  public CreateUnitEvent(Commander commander, UnitModel model, XYCoord coords, AnimationStyle anim, boolean unitIsReady, boolean allowStomping)
  {
    myCommander = commander;
    myBuildCoords = coords;
    myAnimStyle = anim;
    myNewUnit = new Unit(myCommander, model);
    myNewUnit.isTurnOver = !unitIsReady;
    myBoots = allowStomping;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    if( myAnimStyle == AnimationStyle.DROP_IN )
      return mapView.buildAirdropAnimation(myNewUnit, null, myBuildCoords, null);
    return null;
  }

  @Override
  public GameEventQueue sendToListener(GameEventListener listener)
  {
    if( null != myNewUnit )
    {
      return listener.receiveCreateUnitEvent(myNewUnit);
    }
    return null;
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    if( null != myNewUnit )
    {
      myCommander.units.add(myNewUnit);
      if( myFudgeRadius < 1 || myBoots )
        gameMap.addNewUnit(myNewUnit, myBuildCoords.xCoord, myBuildCoords.yCoord, myBoots);
      else
      {
        boolean success = false;
        // Check concentric rings outwards, to spawn as close to the original spot as possible
        for( int radius = 0; !success && radius <= myFudgeRadius; ++radius )
          for( XYCoord xyc : Utils.findLocationsInRange(gameMap, myBuildCoords, radius, radius) )
          {
            Unit resident = gameMap.getResident(xyc);
            if( resident == null )
            {
              gameMap.addNewUnit(myNewUnit, xyc.xCoord, xyc.yCoord, false);
              success = true;
              break;
            }
          }
        if( !success )
          System.out.println("Warning: Failed to find a place to spawn " + myNewUnit
                            + " within " + myFudgeRadius + " tiles of " + myBuildCoords);
      }
      myCommander.army.myView.revealFog(myNewUnit);
    }
    else
    {
      System.out.println("Warning! Attempting to build unit with insufficient funds.");
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return myBuildCoords;
  }

  @Override
  public XYCoord getEndPoint()
  {
    return myBuildCoords;
  }
}
