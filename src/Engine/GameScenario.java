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
    rules = new GameRules(new AWBWUnits(), income, startFunds, FogMode.OFF_DOR, TagMode.OFF);
  }
  public GameScenario(UnitModelScheme scheme, int income, int startFunds, FogMode fog, TagMode tags)
  {
    rules = new GameRules(scheme, income, startFunds, fog, tags);
  }

  public GameEventQueue initTurn(GameMap map)
  {
    return new GameEventQueue();
  }


  public enum TagMode
  {
    OFF       (false, "Dunno what you expected"),
    AWBW      (true , "Multi-CO; only one active at a time"),
    Persistent(true , "Multi-CO; only one builds or COPs at a time"),
    Team_Merge(false, "All teams become a single player");
    // Cartridge

    public final boolean supportsMultiCmdrSelect;
    public final String description;
    private TagMode(boolean multiCO, String desc)
    {
      this.supportsMultiCmdrSelect = multiCO;
      description = desc;
    }
  };

  public enum FogMode
  {
    OFF_TRILOGY(false, false), ON_TRILOGY(true, false), OFF_DOR(false, true), ON_DOR(true, true);

    public final boolean fogDefaultsOn;
    public final boolean dorMode;
    private FogMode(boolean isOn, boolean DoR)
    {
      this.fogDefaultsOn = isOn;
      this.dorMode = DoR;
    }
  };

  /** Object to hold the rules of engagement for a given match. */
  public static class GameRules implements Serializable
  {
    private static final long serialVersionUID = 1L;
    public final int incomePerCity;
    public final int startingFunds;
    public final UnitModelScheme unitModelScheme;
    public final TagMode tagMode;
    public FogMode fogMode;

    public GameRules(UnitModelScheme ums, int income, int startFunds, FogMode fog, TagMode tags)
    {
      incomePerCity = income;
      startingFunds = startFunds;
      unitModelScheme = ums;
      tagMode = tags;
      fogMode = fog;
    }
  }
}
