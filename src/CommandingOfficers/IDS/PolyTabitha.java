package CommandingOfficers.IDS;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Units.Unit;

public class PolyTabitha extends TabithaEngine
{
  private static final long serialVersionUID = 1L;
  public static final int MEGA_ATK = 35;
  public static final int MEGA_DEF = 35;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Polytha");
      infoPages.add(new InfoPage(
            "--POLYTHA--\n"
          + "Can grant a \"Mega Boost\" of "+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"
          + "Gains an extra Mega Boost when COP is charged, and a third at full meter.\n"
          + "Loses 3 stars worth of charge on the loss of any Boosted unit.\n"));
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
    public Commander create(GameScenario.GameRules rules)
    {
      return new PolyTabitha(rules);
    }
  }

  @Override
  public int getMegaBoostCount()
  {
    return 1 + getReadyAbilities().size();
  }
  @Override
  public void onCOULost(Unit minion)
  {
    double max = getMaxAbilityPower();
    double loss = max / 10 * 3; // 10 stars total, 3 to lose
    modifyAbilityPower(-loss);
  }

  public PolyTabitha(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);

    addCommanderAbility(new NukeIt(this, "Firestorm",   6, 4, 10, 10));
    addCommanderAbility(new NukeIt(this, "Apocalypse", 10, 8, 35, 35));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
