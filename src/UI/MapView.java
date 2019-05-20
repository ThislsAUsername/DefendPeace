package UI;

import CommandingOfficers.Commander;
import Engine.GameInstance;
import Engine.IView;
import Engine.MapController;
import Engine.Path;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapWindow;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public abstract class MapView implements IView
{
  // TODO: This doesn't really belong here. The specific artist should handle this, ideally.
  private int unitMoveSpeedMsPerTile = 100;

  protected GameAnimation currentAnimation = null;

  protected MapController mapController = null;
  
  protected GameMap foggedMap;

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
  public GameAnimation buildUnitJoinAnimation()
  {
    return null;
  }
  public GameAnimation buildResupplyAnimation(Unit unit)
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

  protected GameMap getDrawableMap(GameInstance myGame)
  {
    // Here are the fog-drawing rules. If there are:
    //   zero humans - spectating - draw everything the current player sees.
    //   one human - player vs ai - draw everything the human player could see.
    //   2+ humans - player vs player - draw what the current player sees, IFF the player is human.

    // Humans need to see what they can see.
    GameMap gameMap = myGame.activeCO.myView;
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
          foggedMap = new MapWindow(myGame.gameMap, null, myGame.isFogEnabled());
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
  protected GameMap getHumanPlayerMap(GameInstance myGame)
  {
    GameMap map = null;

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