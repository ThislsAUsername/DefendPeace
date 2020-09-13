package Engine;

import java.io.Serializable;

import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Units.AWBWUnits;
import Units.UnitModelScheme;


public class GameScenario implements Serializable
{
  private static final long serialVersionUID = 1L;
  public final static int DEFAULT_INCOME = 1000;
  public final static int DEFAULT_STARTING_FUNDS = 0;

  public final GameRules rules;

  public GameScenario()
  {
    this(DEFAULT_INCOME, DEFAULT_STARTING_FUNDS);
  }
  public GameScenario(int income, int startFunds)
  {
    rules = new GameRules(new AWBWUnits(), income, startFunds, false);
  }
  public GameScenario(UnitModelScheme scheme, int income, int startFunds, boolean isFogEnabled)
  {
    rules = new GameRules(scheme, income, startFunds, isFogEnabled);
  }

  public GameEventQueue initTurn(GameMap map)
  {
    return new GameEventQueue();
  }

  /** Object to hold the rules of engagement for a given match. */
  public static class GameRules implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public final int incomePerCity;
    public final int startingFunds;
    public final UnitModelScheme unitModelScheme;
    public boolean isFogEnabled;

    public GameRules(UnitModelScheme ums, int income, int startFunds, boolean fogOn)
    {
      incomePerCity = income;
      startingFunds = startFunds;
      unitModelScheme = ums;
      isFogEnabled = fogOn;
    }
  }
}
