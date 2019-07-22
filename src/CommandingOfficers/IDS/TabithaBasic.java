package CommandingOfficers.IDS;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;

public class TabithaBasic extends TabithaEngine
{
  private static final CommanderInfo coInfo = new instantiator();

  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Tabitha");
      infoPages.add(new InfoPage(
            "--TABITHA--\n"
          + "Can grant a \"Mega Boost\" of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"
          + MECHANICS_BLURB
          + "xxxxxxXXXX\n"
          + "FIRESTORM: A 2-range missile hits the opponent's largest mass of units and deals 4 HP damage; all units gain +10/10.\n"
          + "APOCALYPSE: A 2-range missile hits the opponent's largest mass of units and deals 8 HP damage; all units gain +35/35."));
    }
    @Override
    public Commander create()
    {
      return new TabithaBasic();
    }
  }

  public int getMegaBoostCount() {return 1;}
  public void onCOULost() {};
  public static int MEGA_ATK = 35;
  public static int MEGA_DEF = 35;

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
