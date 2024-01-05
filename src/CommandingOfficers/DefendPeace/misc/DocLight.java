package CommandingOfficers.DefendPeace.misc;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventQueue;
import UI.UIUtils;
import Units.*;

public class DocLight extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Doctor Light", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.MISC);
      infoPages.add(new InfoPage(
            "Even here, it is not safe. Even this grave has been defaced.\n"
          + "Someone has written on this stone: 'Hope rides alone'\n"));
      infoPages.add(new InfoPage(
          "Base Zone: 0\n" +
          "Can deploy your COU only twice (per game!), with different stats each time (Protoman is first)\n" +
          "Protoman Boost: +"+BLUES_POWER+"/"+BLUES_POWER+" stats and +0/"+BLUES_SHIELD+" vs indirect attack ("+(BLUES_POWER+130)+"/"+(BLUES_POWER+BLUES_SHIELD+130)+" vs indirects)\n" +
          "Megaman Boost: +"+ROCK_POWER+"/"+ROCK_POWER+" stats ("+(ROCK_POWER+130)+"/"+(ROCK_POWER+130)+" total)\n"));
      infoPages.add(new InfoPage(
          "No powers.\n" +
          "A hero is just a man who knows he is free.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
      infoPages.add(new InfoPage(
          "Art by Kayaur"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new DocLight(rules);
    }
  }
  public static final int RADIUS  = 0;
  public static final int BLUES_POWER   = 30;
  public static final int BLUES_SHIELD  = 140;
  public static final char BLUES_MARK   = 'P';

  public static final int ROCK_POWER   = 50;
  public static final char ROCK_MARK   = 'M';

  boolean deployedProto = false;
  boolean deployedRock  = false;

  public DocLight(GameScenario.GameRules rules)
  {
    super(RADIUS, BLUES_POWER, BLUES_POWER, coInfo, rules);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public char getCOUMark()
  {
    if( !deployedRock )
      return BLUES_MARK;
    return ROCK_MARK;
  }
  @Override
  public int getCOUCount()
  {
    if( !deployedRock )
      return 1;
    return 0;
  }

  @Override
  public GameEventQueue receiveDeployCOUEvent(Unit COU, int cost)
  {
    if( !deployedProto )
    {
      deployedProto = true;
      return null;
    }
    deployedRock = true;
    this.zonePow = ROCK_POWER;
    this.zoneDef = ROCK_POWER;

    return super.receiveDeployCOUEvent(COU, cost);
  }

  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    super.modifyUnitDefenseAgainstUnit(params);
    if( isInZone(params.defender) && !deployedRock )
    {
      if( params.battleRange > 1 )
        params.defenseDivision += BLUES_SHIELD;
    }
  }

}
