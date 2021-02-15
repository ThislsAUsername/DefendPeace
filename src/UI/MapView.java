package UI;

import java.util.Collection;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.IView;
import Engine.MapController;
import Engine.Path;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.TeleportEvent;
import Terrain.MapWindow;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public abstract class MapView implements IView
{
  protected GameAnimation currentAnimation = null;

  protected MapController mapController = null;
  
  protected MapWindow foggedMap;

  public void setController(MapController controller)
  {
    mapController = controller;
  }

  public InGameMenu<? extends Object> getCurrentGameMenu()
  {
    return mapController.getCurrentGameMenu();
  }

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
  public GameAnimation buildDemolitionAnimation( StrikeParams params, XYCoord target, int damage )
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
  public GameAnimation buildUnitJoinAnimation()
  {
    return null;
  }
  public GameAnimation buildResupplyAnimation(Unit supplier, Unit unit)
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
  public GameAnimation buildAirdropAnimation( Unit unit, XYCoord start, XYCoord end, Unit obstacle )
  {
    return null;
  }
  public GameAnimation buildTeleportAnimation( Unit unit, XYCoord start, XYCoord end, Unit obstacle )
  {
    return null;
  }
  public GameAnimation buildTurnInitAnimation( Commander cmdr, int turn, boolean fowEnabled, Collection<String> message )
  {
    return null;
  }
  public GameAnimation buildCommanderDefeatAnimation( CommanderDefeatEvent event )
  {
    return null;
  }

  protected MapWindow getDrawableMap(GameInstance myGame)
  {
    // Here are the fog-drawing rules. If there are:
    //   zero humans - spectating - draw everything the current player sees.
    //   one human - player vs ai - draw everything the human player could see.
    //   2+ humans - player vs player - draw what the current player sees, IFF the player is human.

    // Humans need to see what they can see.
    MapWindow gameMap = myGame.activeCO.myView;
    if( myGame.activeCO.isAI() ) // If it's not a human, figure out what to show.
    {
      int numHumans = countHumanPlayers(myGame);
      if( 1 == numHumans )
      {
        // Since there is only one human, always use the human's vision to determine what is drawn.
        gameMap = getHumanPlayerMap(myGame);
      }
      if( myGame.isFogEnabled() && (numHumans > 1) )
      {
        // Hide everything during the AI's turn so the playing field is level.
        if( null == foggedMap )
        {
          foggedMap = new MapWindow(myGame.gameMap, null);
          foggedMap.resetFog();
        }
        gameMap = foggedMap;
      }
    }
    return gameMap;
  }

  /**
   * Returns a count of the number of still-living human players in the game.
   */
  protected int countHumanPlayers(GameInstance myGame)
  {
    int humans = 0;

    for( Commander co : myGame.commanders )
    {
      if( !co.isDefeated && !co.isAI() )
      {
        humans++;
      }
    }
    return humans;
  }

  /**
   * Returns the map owned by the first human Commander found.
   * Intended to be used when there is only one human player.
   */
  protected MapWindow getHumanPlayerMap(GameInstance myGame)
  {
    MapWindow map = null;

    for( Commander co : myGame.commanders )
    {
      if( !co.isDefeated && !co.isAI() )
      {
        map = co.myView;
      }
    }
    return map;
  }
}