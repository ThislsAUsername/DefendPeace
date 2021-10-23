package CommandingOfficers.IDS;
import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;

public class Lyn extends TabithaEngine
{
  private static final long serialVersionUID = 1L;
  public static final int MEGA_ATK = 50;
  public static final int MEGA_DEF = 35;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Lyn");
      infoPages.add(new InfoPage(
            "--Lyn--\n"
          + "No relation to Lin.\n"
          + "Can grant a single \"Mega Boost\" of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until the unit dies.\n"
          + "Boosting works only during COP, and the unit must be on an HQ/lab or matching production property.\n"));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
          HeavenSeal.NAME + " ("+HeavenSeal.COST+"):\n"
        + "Remove any current Boost, and allows allocating a Boost."));
      infoPages.add(new InfoPage(
          SCOP_NAME + " ("+SCOP_COST+"):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 8 HP damage\n"
          + "Unboosted units gain Mega Boost stats."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Lyn(rules);
    }
  }
  public int currentBoostCount = 0;
  private static final String SCOP_NAME = "Angel of Death";
  private static final int SCOP_COST = 7;

  @Override
  public int getMegaBoostCount() {return currentBoostCount;}

  public Lyn(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);
    this.flexibleBoost = false;

    addCommanderAbility(new HeavenSeal(this));
    addCommanderAbility(new NukeIt(this, SCOP_NAME, SCOP_COST, 8, MEGA_ATK, MEGA_DEF));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    currentBoostCount = 0;
    return super.initTurn(map);
  }

  private static class HeavenSeal extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Heaven Seal";
    private static final int COST = 1;
    Lyn COcast;

    HeavenSeal(Lyn commander)
    {
      super(commander, NAME, COST);
      COcast = commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.currentBoostCount = 1;
      COcast.COUs.clear();
    }
  }
}
