package UI;

import java.util.Collection;

import Engine.Army;
import Engine.GameInstance;
import Engine.IView;
import Engine.MapController;
import Engine.GamePath;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.ArmyDefeatEvent;
import Engine.GameEvents.GameEvent;
import Terrain.MapPerspective;
import UI.Art.Animation.GameAnimation;
import Units.Unit;

public abstract class MapView implements IView
{
  protected GameAnimation currentAnimation = null;

  protected MapController mapController = null;
  
  protected MapPerspective foggedMap;

  public void setController(MapController controller)
  {
    mapController = controller;
  }

  public InGameMenu<? extends Object> getCurrentGameMenu()
  {
    return mapController.getCurrentGameMenu();
  }

  public abstract boolean shouldAnimate( GameEvent toAnimate );
  public abstract void animate( GameEvent toAnimate );

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
  public GameAnimation buildMoveAnimation( Unit unit, GamePath movePath )
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
  public GameAnimation buildTurnInitAnimation( Army army, int turn, boolean fowEnabled, Collection<String> message )
  {
    return null;
  }
  public GameAnimation buildCommanderDefeatAnimation( ArmyDefeatEvent event )
  {
    return null;
  }

  protected MapPerspective getDrawableMap(GameInstance myGame)
  {
    if( null == myGame )
      return null;
    // Here are the fog-drawing rules. If there are:
    //   zero humans - spectating - draw everything the current player sees.
    //   one human - player vs ai - draw everything the human player could see.
    //   2+ humans - player vs player - draw what the current player sees, IFF the player is human.

    // Humans need to see what they can see.
    MapPerspective gameMap = myGame.activeArmy.myView;
    if( myGame.activeArmy.isAI() ) // If it's not a human, figure out what to show.
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
          foggedMap = new MapPerspective(myGame.gameMap, null);
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

    for( Army army : myGame.armies )
    {
      if( !army.isDefeated && !army.isAI() )
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
  protected MapPerspective getHumanPlayerMap(GameInstance myGame)
  {
    MapPerspective map = null;

    for( Army army : myGame.armies )
    {
      if( !army.isDefeated && !army.isAI() )
      {
        map = army.myView;
      }
    }
    return map;
  }
}