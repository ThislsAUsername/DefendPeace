package Engine;

import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;


public class GameScenario
{
  public final static int DEFAULT_INCOME = 1000;
  public final static int DEFAULT_STARTING_FUNDS = 0;

  public final GameRules rules;

  public GameScenario()
  {
    this(DEFAULT_INCOME, DEFAULT_STARTING_FUNDS);
  }
  public GameScenario(int income, int startFunds)
  {
    rules = new GameRules(income, startFunds);
  }

  public GameEventQueue initTurn(GameMap map)
  {
    return new GameEventQueue();
  }

  /** Immutable object to hold the rules of engagement for a given match. */
  public static class GameRules
  {
    public final int incomePerCity;
    public final int startingFunds;

    public GameRules(int income, int startFunds)
    {
      incomePerCity = income;
      startingFunds = startFunds;
    }
  }
}
