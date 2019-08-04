package CommandingOfficers.BrennersWolves;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.MapMaster;

public class Isabella extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Isabella");
      infoPages.add(new InfoPage(
          "--ISABELLA--\r\n" + 
          "Units gain +10% firepower.\r\n" + 
          "xxxXXXX\r\n" + 
          "DEEP STRIKE: All units gain +1 movement; all indirects also gain +1 range.\r\n" + 
          "OVERLORD: All units gain +2 movement and +10% defense; all indirects also gain +2 range."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Isabella(rules);
    }
  }

  public Isabella(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    new CODamageModifier(10).applyChanges(this);
    
    addCommanderAbility(new DeepStrike(this));
    addCommanderAbility(new Overlord(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class DeepStrike extends CommanderAbility
  {
    private static final String NAME = "Deep Strike";
    private static final int COST = 3;
    private static final int VALUE = 1;
    Isabella COcast;

    DeepStrike(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Isabella) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(new IndirectRangeBoostModifier(VALUE));
      myCommander.addCOModifier(new COMovementModifier(VALUE));
    }
  }

  private static class Overlord extends CommanderAbility
  {
    private static final String NAME = "Overlord";
    private static final int COST = 7;
    private static final int VALUE = 2;
    Isabella COcast;

    Overlord(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Isabella) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.addCOModifier(new IndirectRangeBoostModifier(VALUE));
      myCommander.addCOModifier(new COMovementModifier(VALUE));
      myCommander.addCOModifier(new CODefenseModifier(10));
    }
  }
}

