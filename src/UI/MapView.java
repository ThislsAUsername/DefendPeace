package UI;

import UI.Art.Animation.GameAnimation;
import Units.Unit;

import Engine.IView;
import Engine.MapController;
import Engine.Path;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;

public abstract class MapView implements IView
{
  // TODO: This doesn't really belong here. The specific artist should handle this, ideally.
  private int unitMoveSpeedMsPerTile = 100;

  protected GameAnimation currentAnimation = null;

  protected MapController mapController = null;

  public void setController(MapController controller)
  {
    mapController = controller;
  }

  public InGameMenu<? extends Object> getCurrentGameMenu()
  {
    return mapController.getCurrentGameMenu();
  }

  /**
   * @return The side-length in pixels of a single map square, taking drawScale into account.
   * NOTE: This assumes that all MapView subclasses will use a square-tile map representation.
   */
  public abstract int getTileSize();

  /**
   * Adds the new events to the queue so they can be animated.
   */
  public abstract void animate( GameEventQueue newEvents );

  public void cancelAnimation()
  {
    if( currentAnimation != null )
    {
      currentAnimation.cancel();
    }
  }
  public double getMapUnitMoveSpeed()
  {
    return unitMoveSpeedMsPerTile;
  }
  public void gameIsOver()
  {
    // Do nothing by default. Subclasses can override.
  }

  /////////////////////////////////////////////////////////////////////////////////////
  ///  The below methods implement the visitor pattern. MapView visits MapEvent so
  ///    that MapEvent can invoke one of these methods to build the correct animation.
  /////////////////////////////////////////////////////////////////////////////////////

  public GameAnimation buildBattleAnimation( BattleSummary summary )
  {
    return null;
  }
  public GameAnimation buildCaptureAnimation()
  {
    return null;
  }
  public GameAnimation buildUnitCombineAnimation()
  {
    return null;
  }
  public GameAnimation buildUnitDieAnimation()
  {
    return null;
  }
  public GameAnimation buildGameOverAnimation()
  {
    return null;
  }
  public GameAnimation buildLoadAnimation()
  {
    return null;
  }
  public GameAnimation buildUnloadAnimation()
  {
    return null;
  }
  public GameAnimation buildMoveAnimation( Unit unit, Path movePath )
  {
    return null;
  }
  public GameAnimation buildCommanderDefeatAnimation( CommanderDefeatEvent event )
  {
    return null;
  }
}