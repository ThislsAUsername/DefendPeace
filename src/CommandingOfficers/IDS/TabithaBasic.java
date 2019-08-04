package CommandingOfficers.IDS;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;

public class TabithaBasic extends TabithaEngine
{
  public static final int MEGA_ATK = 35;
  public static final int MEGA_DEF = 35;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Tabitha");
      infoPages.add(new InfoPage(
            "--TABITHA--\n"
          + "Can grant a single \"Mega Boost\" of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
            "FIRESTORM (6):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 4 HP damage\n"
          + "All units gain +10/10.\n"));
      infoPages.add(new InfoPage(
            "APOCALYPSE (10):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 8 HP damage\n"
          + "All units gain +35/35."));
    }
    @Override
    public Commander create()
    {
      return new TabithaBasic();
    }
  }

  @Override
  public int getMegaBoostCount() {return 1;}

  public TabithaBasic()
  {
    super(MEGA_ATK, MEGA_DEF, coInfo);

    addCommanderAbility(new NukeIt(this, "Firestorm",   6, 4, 10, 10));
    addCommanderAbility(new NukeIt(this, "Apocalypse", 10, 8, 35, 35));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
