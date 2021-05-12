package AI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Patch;
import Engine.GameAction;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.Environment.Weathers;
import Terrain.MapInfo;
import Terrain.MapLibrary;
import Terrain.MapMaster;
import UI.UIUtils;

public class FightClub
{
  public static void main(String[] args)
  {
    MapLibrary.getMapList();
    System.out.println();

    // Select map(s).
    //List<MapInfo> maps = MapLibrary.getMapList();
//    List<MapInfo> maps = Arrays.asList(MapLibrary.getByName("Firing Range"), MapLibrary.getByName("Aria of War"));
    List<MapInfo> maps = Arrays.asList(MapLibrary.getByName("Firing Range"),
                                       MapLibrary.getByName("Shadows Chase You Endlessly"),
                                       MapLibrary.getByName("Blood on my Hands"),
                                       MapLibrary.getByName("Aria of War"));
    // How many bouts per map?
    int numGamesPerSet = 3;
    // Select CO(s).
    List<CommanderInfo> COs = Arrays.asList(Patch.getInfo(), Patch.getInfo());
    // Select AI(s).
    List<AIMaker> AIs = Arrays.asList(Muriel.info, WallyAI.info);

    // Run a set of games on each map.
    for( int setNum = 0; setNum < maps.size(); ++setNum )
    {
      MapInfo setMap = maps.get(setNum);
      System.out.println("Starting set " + setNum + " on " + setMap.mapName);
      GameSet set = new GameSet(new GameSetParams(setMap, numGamesPerSet, COs, AIs));
      set.run();
    }

    System.out.println("All sets complete!");
  }

  static class ContestantInfo
  {
    AIMaker myAi;
    CommanderInfo myCo;
    ContestantInfo(CommanderInfo co, AIMaker ai)
    {
      myCo = co;
      myAi = ai;
    }
  }

  static class GameSetResults
  {
    String mapName;
    HashMap<ContestantInfo, Integer> scores = new HashMap<ContestantInfo, Integer>();

    public GameSetResults(String mapName, List<ContestantInfo> contestants)
    {
      this.mapName = mapName;
      for(ContestantInfo cc : contestants)
        scores.put(cc, 0);
    }

    public void incrementScore(ContestantInfo cInfo)
    {
      scores.put(cInfo, scores.get(cInfo) + 1);
    }

    @Override
    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      ContestantInfo[] cInfos = scores.keySet().toArray(new ContestantInfo[0]);
      sb.append(String.format("%s (%s)", cInfos[0].myAi.getName(), cInfos[0].myCo.name));
      for( int cc = 1; cc < cInfos.length; ++cc)
        sb.append(String.format(" vs %s (%s)", cInfos[cc].myAi.getName(), cInfos[cc].myCo.name));
      sb.append(String.format(" on %s\n", mapName));
      sb.append(String.format("  %d", scores.get(cInfos[0])));
      for( int cc = 1; cc < cInfos.length; ++cc)
        sb.append(String.format(" to %d", scores.get(cInfos[cc])));
      sb.append('\n');
      return sb.toString();
    }
  }

  static class GameSetParams
  {
    // Primary settings; must be provided via the constructor.
    MapInfo mapInfo;
    int numGames = 3;
    List<CommanderInfo> COs;
    List<AIMaker> AIs;

    // Additional settings to mess with. Optional to provide, but public so defaults can be overridden.
    boolean isFogOn = false;
    Weathers defaultWeather = Weathers.CLEAR;

    public GameSetParams(MapInfo map, int nGames, List<CommanderInfo> cos, List<AIMaker> ais)
    {
      mapInfo = map;
      numGames = nGames;
      COs = cos;
      AIs = ais;
    }
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
        public void write(int b) throws IOException{}
      }));

      MapInfo mi = params.mapInfo;
      List<ContestantInfo> contestants = new ArrayList<ContestantInfo>();
      for( int cc = 0; cc < params.COs.size(); cc++)
        contestants.add(new ContestantInfo(params.COs.get(cc), params.AIs.get(cc)));
      GameSetResults results = new GameSetResults(mi.mapName, contestants);

      for( int gameIndex = 0; gameIndex < params.numGames; ++gameIndex )
      {
        GameScenario scenario = new GameScenario(mi.getValidUnitModelSchemes()[0],
            GameScenario.DEFAULT_INCOME, GameScenario.DEFAULT_STARTING_FUNDS, false);

        int numCos = mi.getNumCos();

        // Create all of the combatants.
        HashMap<Integer, ContestantInfo> teamMapping = new HashMap<Integer, ContestantInfo>(); // TODO: This currently doesn't work for team games. 
        List<Commander> combatants = new ArrayList<Commander>();
        // Offset cc by gameIndex to rotate the contestant starting locations.
        for( int cc = gameIndex; cc < (gameIndex + contestants.size()); ++cc){
          int ci = cc % contestants.size();
          ContestantInfo cInfo = contestants.get(ci);
          Commander com = cInfo.myCo.create(scenario.rules);
          com.team = ci;
          com.myColor = UIUtils.getCOColors()[ci];
          com.faction = UIUtils.getFactions()[ci];
          com.setAIController(cInfo.myAi.create(com));
          combatants.add(com);
          teamMapping.put(ci, cInfo);
        }

        if( numCos != combatants.size() )
        {
          defaultOut.println(String.format("WARNING: Wrong number of COs specified for this map (expected %d, got %d)!", numCos, combatants.size()));
          return;
        }

        defaultOut.println("  Starting game " + gameIndex + " on map " + mi.mapName + " with combatants:");
        for( int i = 0; i < numCos; ++i )
          defaultOut.println("    Team " + combatants.get(i).team + ": "
                               + combatants.get(i).getControllerName() + " controlling " + combatants.get(i).coInfo.name);

        // Build the CO list and the new map and create the game instance.
        MapMaster map = new MapMaster(combatants.toArray(new Commander[0]), mi);
        GameInstance newGame = null;
        if( map.initOK() )
        {
          newGame = new GameInstance(map, params.defaultWeather, scenario, false);
        }

        GameResults gameResults = runGame(newGame, defaultOut);
        List<Commander> winners = gameResults.winners;
        int winningTeam = winners.get(0).team;
        defaultOut.println("  Game " + gameIndex + " Results:");
        defaultOut.println(gameResults);
//        defaultOut.println("Winners:");
//        for( Commander winner : winners )
//          defaultOut.println("\t" + winner.coInfo.name);

        results.incrementScore(teamMapping.get(winningTeam));
      }

      defaultOut.println("Set results:");
      defaultOut.println(results);
      System.setOut(defaultOut);
    }

    public static class GameResults
    {
      public static enum EndCondition{
        UNKNOWN,
        CONQUEST,
        TURN_LIMIT
      }

      List<Commander> winners, contestants;
      int winningTeam;
      int numTurns;
      EndCondition endReason;
      Long totalGameTimeNanos;
      HashMap<Commander, Long> stopwatches;

      public GameResults(List<Commander> victors, List<Commander> players, int nTurns, EndCondition reason,
          Long gameRunTime, HashMap<Commander, Long> playerRunTimes)
      {
        winners = victors;
        contestants = players;
        winningTeam = winners.get(0).team;
        numTurns = nTurns;
        endReason = reason;
        totalGameTimeNanos = gameRunTime;
        stopwatches = playerRunTimes;
      }

      @Override
      public String toString()
      {
        StringBuffer sb = new StringBuffer();
        double ns2s = 1./1000000000;
        DecimalFormat df = new DecimalFormat("#.##");

        sb.append("    Team ").append(winningTeam).append(" wins by ").append(endReason).append(" after ").append(numTurns).append(" turns.\n")
          .append("    Game took ").append(df.format(totalGameTimeNanos * ns2s)).append(" seconds").append('\n');
        double totalThinkTimeNanos = 0;
        for( Long thinkTime : stopwatches.values() )
        {
          totalThinkTimeNanos += thinkTime;
        }
        String thinkPct = df.format(100*(totalThinkTimeNanos / totalGameTimeNanos));
        String thinkTime = df.format(totalThinkTimeNanos * ns2s);
        sb.append("    Thinking comprised ").append(thinkPct).append("% (").append(thinkTime).append("s) of the run time\n");
        for( Commander co : contestants )
        {
          String coPct = df.format(100 * (stopwatches.get(co) / totalThinkTimeNanos));
          String coTime = df.format(stopwatches.get(co) * ns2s);
          sb.append("      ").append(co.getControllerName()).append(" (").append(co.coInfo.name).append("): ")
            .append("Used ").append(coPct).append("% (").append(coTime).append("s) of the thinking time.\n");
        }

        return sb.toString();
      }
    }

    /**
     * @return The winning team
     */
    public GameResults runGame(GameInstance game, PrintStream defaultOut)
    {
      long gameRunTimeNanos = System.nanoTime();
      HashMap<Commander, Long> stopwatches = new HashMap<Commander, Long>();
      for( Commander co : game.commanders )
      {
        stopwatches.put(co, 0L);
      }
      GameResults.EndCondition endReason = GameResults.EndCondition.UNKNOWN;

      boolean isGameOver = false;
      while (!isGameOver)
      {
        startNextTurn(game, defaultOut);

        GameEventQueue actionEvents = new GameEventQueue();
        boolean endAITurn = false;
        long thinkTimeNanos = 0;
        while (!endAITurn && !isGameOver)
        {
          long thinkStartNanos = System.nanoTime();
          GameAction aiAction = game.activeCO.getNextAIAction(game.gameMap);
          thinkTimeNanos += System.nanoTime() - thinkStartNanos;
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
          if( activeNum < 2 )
          {
            isGameOver = true;
            endReason = GameResults.EndCondition.CONQUEST;
          }
        }
        stopwatches.put(game.activeCO, stopwatches.get(game.activeCO) + thinkTimeNanos);

        // Map should-ish be covered in units by turncount == map area
        if(game.getCurrentTurn() > game.gameMap.mapWidth * game.gameMap.mapHeight)
        {
          if(game.commanders[0].units.size()/2 > game.commanders[1].units.size() )
          {
            game.commanders[1].isDefeated = true;
            isGameOver = true;
            endReason = GameResults.EndCondition.TURN_LIMIT;
          }
          if(game.commanders[1].units.size()/2 > game.commanders[0].units.size() )
          {
            game.commanders[0].isDefeated = true;
            isGameOver = true;
            endReason = GameResults.EndCondition.TURN_LIMIT;
          }
        }
      }

      gameRunTimeNanos = System.nanoTime() - gameRunTimeNanos;

      ArrayList<Commander> winners = new ArrayList<Commander>();
      for( int i = 0; i < game.commanders.length; ++i )
      {
        if( !game.commanders[i].isDefeated )
          winners.add(game.commanders[i]);
      }
      return new GameResults(winners, Arrays.asList(game.commanders), game.getCurrentTurn(), endReason, gameRunTimeNanos, stopwatches);
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
        eventQueue.addAll(GameEventListener.publishEvent(event, game));
      }
    }
  } // ~GameSet

}
