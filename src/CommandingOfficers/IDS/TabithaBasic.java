package CommandingOfficers.IDS;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;

public class TabithaBasic extends TabithaEngine
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
      super("Tabitha");
      infoPages.add(new InfoPage(
            "--TABITHA--\n"
          + "Can grant a single \"Mega Boost\" of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until the unit dies.\n"
          + "To Boost, the unit must be on an HQ/lab or matching production property.\n"));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
            "FIRESTORM (6):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 4 HP damage\n"
          + "Unboosted units gain +10/10.\n"));
      infoPages.add(new InfoPage(
            "APOCALYPSE (10):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 8 HP damage\n"
          + "Unboosted units gain Mega Boost stats."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new TabithaBasic(rules);
    }
  }

  @Override
  public int getMegaBoostCount() {return 1;}

  public TabithaBasic(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);
    this.flexibleBoost = false;

    addCommanderAbility(new NukeIt(this, "Firestorm",   6, 4, 10, 10));
    addCommanderAbility(new NukeIt(this, "Apocalypse", 10, 8, MEGA_ATK, MEGA_DEF));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
