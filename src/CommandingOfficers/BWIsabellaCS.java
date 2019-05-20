package CommandingOfficers;

import java.util.Map;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class BWIsabellaCS extends Commander
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
          "OVERLORD: All units gain +2 movement; all indirects also gain +2 range."));
    }
    @Override
    public Commander create()
    {
      return new BWIsabellaCS();
    }
  }

  public BWIsabellaCS()
  {
    super(coInfo);

    new CODamageModifier(10).apply(this);
    
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
    BWIsabellaCS COcast;

    DeepStrike(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BWIsabellaCS) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(COcast, VALUE);

      COMovementModifier moveMod = new COMovementModifier(VALUE);

      for( UnitModel um : COcast.unitModels )
      {
          moveMod.addApplicableUnitModel(um);
      }

      COcast.addCOModifier(rangeBoost);
      myCommander.addCOModifier(moveMod);
    }
  }

  private static class Overlord extends CommanderAbility
  {
    private static final String NAME = "Overlord";
    private static final int COST = 7;
    private static final int VALUE = 2;
    BWIsabellaCS COcast;

    Overlord(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BWIsabellaCS) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(COcast, VALUE);

      COMovementModifier moveMod = new COMovementModifier(VALUE);

      for( UnitModel um : COcast.unitModels )
      {
          moveMod.addApplicableUnitModel(um);
      }

      COcast.addCOModifier(rangeBoost);
      myCommander.addCOModifier(moveMod);
    }
  }
}

