package UI;

import java.util.Queue;

import UI.Art.Animation.GameAnimation;

import Engine.GameAction;
import Engine.IView;
import Engine.MapController;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventSequence;

public abstract class MapView implements IView
{
  public GameAction currentAction = null;
  private Queue<GameEvent> eventsToAnimate = new GameEventSequence();

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
  public void animate( GameEventSequence newEvents )
  {
    eventsToAnimate.addAll( newEvents );
  }

  /**
   * Orders the MapView to animate the MapEvents in the given GameAction.
   */
  @Deprecated
  public void animate_old(GameAction action)
  {
    if( currentAction != null && currentAction != action )
    {
      // Typically, this will be called either for a player action (in which case currentAction
      //  should have been populated through UI interaction, or for an AI action, in which case
      //  currentAction will still be null (because AIs don't use UIs).
      System.out.println("WARNING! Animating an unexpected action!");
    }

    currentAction = action;

    // If we have a previous animation in progress, cancel it to start the new one.
    // Note that this really shouldn't happen in practice, but we check for safety.
    if( currentAnimation != null )
    {
      currentAnimation.cancel();
      eventsToAnimate.clear();
      currentAnimation = null;
    }

    // Add the list of mapEvents for animating later.
    //eventsToAnimate.addAll( action.getMapEvents() );

    // Get the first event and its animation.
    GameEvent event = eventsToAnimate.peek();
    currentAnimation = event.getEventAnimation( this );

    if( currentAnimation == null )
    {
      // Animation for this action is not supported. Just let the controller know.
      mapController.animationEnded( event, eventsToAnimate.isEmpty() );
    }
  }
  public boolean isAnimating()
  {
    return currentAnimation != null;
  }
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
  public GameAnimation buildMoveAnimation()
  {
    return null;
  }
  public GameAnimation buildCommanderDefeatAnimation( CommanderDefeatEvent event )
  {
    return null;
  }
}