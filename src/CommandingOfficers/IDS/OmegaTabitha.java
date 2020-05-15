package CommandingOfficers.IDS;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Units.Unit;

public class OmegaTabitha extends TabithaEngine
{
  private static final long serialVersionUID = 1L;
  public static final int MEGA_ATK = 70;
  public static final int MEGA_DEF = 45;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Omega Tabitha");
      infoPages.add(new InfoPage(
            "The strongest reasonable interpretation of Tabitha into AWBW mechanics.\n"
          + "Can grant a single \'Mega Boost\' of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"
          + "Loses all COP charge if the Mega Boosted unit dies."));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
            "APOCALYPSE (13):\n"
          + "A 2-range missile hits the opponent's largest mass of units and deals 8 HP damage\n"
          + "All units gain +50/35."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new OmegaTabitha(rules);
    }
  }

  @Override
  public int getMegaBoostCount() {return 1;}
  @Override
  public void onCOULost(Unit minion)
  {
    modifyAbilityPower(-42);
  }

  public OmegaTabitha(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);

    addCommanderAbility(new NukeIt(this, "APOCALYPSE", 13, 8, 50, 35));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
