package CommandingOfficers;

import java.util.Map;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Terrain.GameMap;
import Units.UnitModel;

public class BWIsabellaBasic extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Isabella\nBasic", new instantiator());
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BWIsabellaBasic();
    }
  }

  public BWIsabellaBasic()
  {
    super(coInfo);

    new CODamageModifier(10).apply(this);
    
    addCommanderAbility(new Overlord(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class Overlord extends CommanderAbility
  {
    private static final String NAME = "Deep Strike";
    private static final int COST = 7;
    private static final int VALUE = 2;
    BWIsabellaBasic COcast;

    Overlord(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BWIsabellaBasic) commander;
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      IndirectRangeBoostModifier rangeBoost = new IndirectRangeBoostModifier(VALUE);

      Map<UnitModel, UnitModel> indirects = rangeBoost.init(COcast);

      COMovementModifier moveMod = new COMovementModifier(VALUE);

      for( UnitModel um : COcast.unitModels )
      {
          moveMod.addApplicableUnitModel(um);
      }
      for( UnitModel um : indirects.values() )
      {
          moveMod.addApplicableUnitModel(um);
      }

      COcast.addCOModifier(rangeBoost);
      myCommander.addCOModifier(moveMod);
    }
  }
}
