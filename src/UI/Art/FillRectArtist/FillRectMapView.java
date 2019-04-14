package UI.Art.FillRectArtist;

import java.awt.Dimension;
import java.awt.Graphics;

import Engine.GameInstance;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import UI.Art.Animation.NobunagaBattleAnimation;

public class FillRectMapView extends MapView
{
  private FillRectMapArtist mapArtist;
  private FillRectUnitArtist unitArtist;
  private FillRectMenuArtist menuArtist;

  private int baseTileSize = 16;
  private int drawScale = 2;
  private int mapViewWidth;
  private int mapViewHeight;

  GameEventQueue eventSequence = null;
  GameEvent currentEvent = null;

  public FillRectMapView(GameInstance game)
  {
    mapViewWidth = baseTileSize * drawScale * 15;
    mapViewHeight = baseTileSize * drawScale * 10;

    mapArtist = new FillRectMapArtist(game);
    unitArtist = new FillRectUnitArtist(game);
    menuArtist = new FillRectMenuArtist(game);

    mapArtist.setView(this);
    unitArtist.setView(this);
    menuArtist.setView(this);

    eventSequence = new GameEventQueue();
  }

  @Override
  public Dimension getPreferredDimensions()
  {
    return new Dimension(mapViewWidth, mapViewHeight);
  }

  @Override
  public void setPreferredDimensions(int width, int height)
  {
    // ignore!
  }

  @Override
  public int getTileSize()
  {
    return baseTileSize * drawScale;
  }

  public int getViewWidth()
  {
    return mapViewWidth;
  }

  public int getViewHeight()
  {
    return mapViewHeight;
  }

  @Override
  public void render(Graphics g)
  {
    mapArtist.drawMap(g);
    mapArtist.drawHighlights(g);
    if( mapController.getContemplatedMove() != null )
    {
      mapArtist.drawMovePath(g, mapController.getContemplatedMove());
    }
    unitArtist.drawUnits(g, mapController.getContemplatedActor(), mapController.getContemplatedMove());

    // If we have a new event to animate, load the animation.
    if( currentEvent != null && currentAnimation == null )
    {
      while( null == currentAnimation && !eventSequence.isEmpty() )
      {
        currentAnimation = currentEvent.getEventAnimation( this );
        if( null == currentAnimation )
        {
          mapController.animationEnded( currentEvent, eventSequence.isEmpty() );
          currentEvent = eventSequence.poll();
        }
      }
    }

    if( currentAnimation != null )
    {
      // Animate until it tells you it's done.
      if( currentAnimation.animate(g) )
      {
        mapController.animationEnded( currentEvent, eventSequence.isEmpty() );
        currentEvent = null;
        currentAnimation = null;
        loadNextEventAnimation(); // Load the next event into the hopper.
      }
    }
    else if( getCurrentGameMenu() == null )
    {
      mapArtist.drawCursor(g);
    }
    else
    {
      menuArtist.drawMenu(g);
    }
  }

  @Override
  public void animate(GameEventQueue newEvents)
  {
    if( null != newEvents )
    {
      eventSequence = newEvents;

      // Cancel any current animation.
      if( null != currentAnimation )
      {
        currentAnimation.cancel();
        currentAnimation = null;
      }

      // Pop the first new event.
      loadNextEventAnimation();

      // If we can't animate any of the incoming events, just release control.
      if( null == currentAnimation )
      {
        mapController.animationEnded(null, true);
      }
    }
    else
    {
      mapController.animationEnded(null, true);
    }
  }

  /**
   * Utility function to get the animation for the next animatable GameEvent
   * in the GameEvent queue.
   */
  private void loadNextEventAnimation()
  {
    // Keep pulling events off the queue until we get one we can draw.
    while( null == currentAnimation && !eventSequence.isEmpty() )
    {
      currentEvent = eventSequence.poll();
      if( null != currentEvent )
      {
        currentAnimation = currentEvent.getEventAnimation(this);
        if( null == currentAnimation )
        {
          // There isn't an animation for this event. Just notify the controller.
          mapController.animationEnded(currentEvent, eventSequence.isEmpty());
        }
      }
    }
  }

  public GameAnimation buildBattleAnimation( BattleSummary summary )
  {
    return new NobunagaBattleAnimation(getTileSize(), summary.attacker.x, summary.attacker.y, summary.defender.x, summary.defender.y);
  }

  @Override
  public void cleanup()
  {
    // No cleanup required
  }
}
