package AI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderLibrary;
import CommandingOfficers.Patch;
import CommandingOfficers.Strong;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GameScenario.GameRules;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.Environment.Weathers;
import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.UnitModelScheme;

public class FightClub
{
  public static void main(String[] args)
  {
    MapLibrary.getMapList();
    System.out.println();
    int setsToRun = 3;

    for( int setNum = 0; setNum < setsToRun; ++setNum )
    {
      System.out.println("Starting set: " + setNum);
      GameSet set = new GameSet(new GameSetParams());
      set.params.randomSeed += setNum;
      set.run();
    }

    System.out.println("All sets complete!");
  }

  static class GameSetParams
  {
//    List<CommanderInfo> COs = CommanderLibrary.getCommanderList();
    List<CommanderInfo> COs = Arrays.asList(Patch.getInfo());
    boolean allowMirrorCOs = true;
    boolean rePickCOs = false;

    //  List<AIMaker> AIs = AILibrary.getAIList().subList(1, AILibrary.getAIList().size());
//    List<AIMaker> AIs = Arrays.asList(InfantrySpamAI.info, Muriel.info, WallyAI.info);
    List<AIMaker> AIs = Arrays.asList(WallyAI.info);
    boolean allowMirrorAIs = true;
    boolean rePickAIs = false;

    //  List<MapInfo> maps = MapLibrary.getMapList();
    List<MapInfo> maps = Arrays.asList(MapLibrary.getByName("Firing Range"));
//    List<MapInfo> maps = Arrays.asList(MapLibrary.getByName("Shadows Chase You Endlessly"),
//                                       MapLibrary.getByName("Blood on my Hands"),
//                                       MapLibrary.getByName("Aria of War"));
    boolean rematchOnSameMap = true;

    int startFunds = GameScenario.DEFAULT_STARTING_FUNDS;
    int income = GameScenario.DEFAULT_INCOME;
    boolean isFogOn = false;
    Weathers defaultWeather = Weathers.CLEAR;

    int gamesPerSet = 3;
    int randomSeed = 42;
  }

  static class GameSet
  {
    GameSetParams params;

    public GameSet(GameSetParams gameSetParams)
    {
      params = gameSetParams;
    }

    public void run()
    {
      // suppress normal printing to avoid spam and speed stuff up a lil'
      PrintStream defaultOut = System.out;
      System.setOut(new PrintStream(new OutputStream(){
        @Override
        public void write(int b) throws IOException
        {
          // TODO Auto-generated method stub
        }
      }));
      Random rand = new Random(params.randomSeed);

      MapInfo mi = params.maps.get(rand.nextInt(params.maps.size()));

      List<AIMaker> gameAIs = new ArrayList<AIMaker>();
      List<CommanderInfo> gameCOs = new ArrayList<CommanderInfo>();

      for( int gameIndex = 0; gameIndex < params.gamesPerSet; ++gameIndex )
      {
        UnitModelScheme[] umSchemes = mi.getValidUnitModelSchemes();
        UnitModelScheme ums = umSchemes[rand.nextInt(umSchemes.length)];

        GameScenario scenario = new GameScenario(ums, GameScenario.DEFAULT_INCOME, GameScenario.DEFAULT_STARTING_FUNDS, params.isFogOn);

        int numCos = mi.getNumCos();

        // Create all of the combatants.
        List<Commander> combatants = buildCombatants(numCos, rand, scenario.rules, gameAIs, gameCOs, defaultOut);

        defaultOut.println("  Starting game on map " + mi.mapName + " with combatants:");
        for( int i = 0; i < numCos; ++i )
          defaultOut.println("    team " + combatants.get(i).team + ": "
                               + gameAIs.get(i).getName() + " controlling " + combatants.get(i).coInfo.name);

        // Build the CO list and the new map and create the game instance.
        MapMaster map = new MapMaster(combatants.toArray(new Commander[0]), mi);
        GameInstance newGame = null;
        if( map.initOK() )
        {
          newGame = new GameInstance(map, params.defaultWeather, scenario, false);
        }

        List<Commander> winners = runGame(newGame, defaultOut);
        defaultOut.println("  Game " + gameIndex + " complete; winning team is: " + winners.get(0).team);
        defaultOut.println();
//        defaultOut.println("Winners:");
//        for( Commander winner : winners )
//          defaultOut.println("\t" + winner.coInfo.name);

        if( !params.rematchOnSameMap )
          mi = params.maps.get(rand.nextInt(params.maps.size()));
      }

      System.setOut(defaultOut);
    }
    
    List<Commander> buildCombatants(int numCOs, Random rand, GameRules rules, List<AIMaker> gameAIs, List<CommanderInfo> gameCOs, PrintStream defaultOut)
    {
      if( (!params.rePickAIs && !gameAIs.isEmpty() && gameAIs.size() != numCOs)
          || (!params.rePickCOs && !gameCOs.isEmpty() && gameCOs.size() != numCOs) )
      {
        defaultOut.println("WARNING: Quantity of COs/AIs doesn't match map; regenerating");
        gameAIs.clear();
        gameCOs.clear();
      }

      if( gameAIs.isEmpty() || params.rePickAIs )
        while (gameAIs.size() < numCOs)
        {
          AIMaker ai = params.AIs.get(rand.nextInt(params.AIs.size()));
          if( params.allowMirrorAIs || !gameAIs.contains(ai) )
            gameAIs.add(ai);
        }

      if( gameCOs.isEmpty() || params.rePickCOs )
        while (gameCOs.size() < numCOs)
        {
          CommanderInfo coI = params.COs.get(rand.nextInt(params.COs.size()));
          if( !params.allowMirrorCOs && gameCOs.contains(coI) )
            continue;
          gameCOs.add(coI);
        }

      List<Commander> combatants = new ArrayList<Commander>(numCOs);
      while (combatants.size() < numCOs)
      {
        Commander co = gameCOs.get(combatants.size()).create(rules);
        co.myColor = UIUtils.getCOColors()[rand.nextInt(UIUtils.getCOColors().length)];
        co.faction = UIUtils.getFactions()[rand.nextInt(UIUtils.getFactions().length)];
        co.team = combatants.size();

        co.setAIController(gameAIs.get(combatants.size()).create(co));
        combatants.add(co);
      }

      return combatants;
    }

    /**
     * @return The winning team
     */
    public List<Commander> runGame(GameInstance game, PrintStream defaultOut)
    {
      boolean isGameOver = false;
      while (!isGameOver)
      {
        startNextTurn(game, defaultOut);

        GameEventQueue actionEvents = new GameEventQueue();
        boolean endAITurn = false;
        while (!endAITurn && !isGameOver)
        {
          GameAction aiAction = game.activeCO.getNextAIAction(game.gameMap);
          if( aiAction != null )
          {
            if( !executeGameAction(aiAction, actionEvents, game, defaultOut) )
            {
              // If aiAction fails to execute, the AI's turn is over. We don't want
              // to waste time getting more actions if it can't build them properly.
              defaultOut.println("WARNING! AI Action " + aiAction.toString() + " Failed to execute!");
              endAITurn = true;
            }
          }
          else
          {
            endAITurn = true;
          }

          while (!isGameOver && !actionEvents.isEmpty())
          {
            executeEvent(actionEvents.poll(), actionEvents, game, defaultOut);
          }

          // If we are done animating the last action, check to see if the game is over.
          // Count the number of COs that are left.
          int activeNum = 0;
          for( int i = 0; i < game.commanders.length; ++i )
          {
            if( !game.commanders[i].isDefeated )
            {
              activeNum++;
            }
          }

          // If fewer than two COs yet survive, the game is over.
          isGameOver = activeNum < 2;
        }

        // Map should-ish be covered in units by turncount == map area
        if(game.getCurrentTurn() > game.gameMap.mapWidth * game.gameMap.mapHeight)
        {
          if(game.commanders[0].units.size()/2 > game.commanders[1].units.size() )
          {
            game.commanders[1].isDefeated = true;
            isGameOver = true;
          }
          if(game.commanders[1].units.size()/2 > game.commanders[0].units.size() )
          {
            game.commanders[0].isDefeated = true;
            isGameOver = true;
          }
        }
      }

      ArrayList<Commander> winners = new ArrayList<Commander>();
      for( int i = 0; i < game.commanders.length; ++i )
      {
        if( !game.commanders[i].isDefeated )
          winners.add(game.commanders[i]);
      }
      return winners;
    }

    /**
     * Execute the provided action and evaluate any aftermath.
     */
    private boolean executeGameAction(GameAction action, GameEventQueue eventQueue, GameInstance game, PrintStream defaultOut)
    {
      boolean actionOK = false; // Not sure if it's a well-formed action yet.
      if( null != action )
      {
        // Compile the GameAction to its component events.
        GameEventQueue events = action.getEvents(game.gameMap);

        if( events.size() > 0 )
        {
          actionOK = true; // Invalid actions don't produce events.
          eventQueue.addAll(events);
        }
      }
      else
      {
        defaultOut.println("WARNING! Attempting to execute null GameAction.");
      }
      return actionOK;
    }

    private void startNextTurn(GameInstance game, PrintStream defaultOut)
    {
      // Tell the game a turn has changed. This will update the active CO.
      GameEventQueue turnEvents = new GameEventQueue();
      boolean turnOK = game.turn(turnEvents);
      if( !turnOK )
      {
        defaultOut.println("WARNING: Turn init failed for some reason");
      }

      while (!turnEvents.isEmpty())
      {
        executeEvent(turnEvents.pop(), turnEvents, game, defaultOut);
      }

//      defaultOut.println("Started turn " + game.getCurrentTurn() + " for CO " + game.activeCO.coInfo.name);
//      defaultOut.println(new COStateInfo(game.activeCO.myView, game.activeCO).getFullStatus());
    }

    public void executeEvent(GameEvent event, GameEventQueue eventQueue, GameInstance game, PrintStream defaultOut)
    {
      if( null != event )
      {
        event.performEvent(game.gameMap);

        // Now that the event has been completed, let the world know.
        GameEventListener.publishEvent(event, game);
      }

      for( Commander co : game.commanders )
        co.pollForEvents(eventQueue);
    }
  } // ~GameSet

}
