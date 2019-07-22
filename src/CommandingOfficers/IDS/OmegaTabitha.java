package CommandingOfficers.IDS;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Units.Unit;

public class OmegaTabitha extends TabithaEngine
{
  public static final int MEGA_ATK = 70;
  public static final int MEGA_DEF = 45;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Omega Tabitha");
      infoPages.add(new InfoPage(
            "Called \"Omega\" because she's extra fair and balanced.\n"
          + "Can grant a single \"Mega Boost\" of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"
          + "Loses all COP charge if the Mega Boosted unit dies."));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
            "APOCALYPSE (13):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 8 HP damage\n"
          + "All units gain +50/35."));
    }
    @Override
    public Commander create()
    {
      return new OmegaTabitha();
    }
  }

  @Override
  public int getMegaBoostCount() {return 1;}
  @Override
  public void onCOULost(Unit minion)
  {
    modifyAbilityPower(-42);
  }

  public OmegaTabitha()
  {
    super(MEGA_ATK, MEGA_DEF, coInfo);

    addCommanderAbility(new NukeIt(this, "APOCALYPSE", 13, 8, 50, 35));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
