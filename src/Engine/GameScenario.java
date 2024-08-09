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
  public final static int DEFAULT_UNIT_CAP = 50;

  public final GameRules rules;

  public GameScenario()
  {
    this(new AWBWUnits(), DEFAULT_INCOME, DEFAULT_STARTING_FUNDS, DEFAULT_UNIT_CAP, FogMode.OFF_DOR, TagMode.OFF);
  }
  public GameScenario(UnitModelScheme scheme, int income, int startFunds, int units, FogMode fog, TagMode tags)
  {
    rules = new GameRules(scheme, income, startFunds, units, fog, tags);
  }

  public GameEventQueue initTurn(GameMap map)
  {
    return new GameEventQueue();
  }


  public enum TagMode
  {
    OFF       (false, "Dunno what you expected"),
    AWBW      (true , "Multi-CO; the current CO owns everything; other COs get half charge free"),
    Persistent(true , "Multi-CO; the current CO builds and COPs; other COs keep units and steal half charge"),
    Team_Merge(false, "All teams become a single player");
    // Cartridge (true, "AWBW, but with affinity bonuses and tag powers"),

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
    public final int unitCap;
    public final UnitModelScheme unitModelScheme;
    public final TagMode tagMode;
    public FogMode fogMode;

    public GameRules(UnitModelScheme ums, int income, int startFunds, int units, FogMode fog, TagMode tags)
    {
      incomePerCity = income;
      startingFunds = startFunds;
      unitCap = units;
      unitModelScheme = ums;
      tagMode = tags;
      fogMode = fog;
    }
  }
}
