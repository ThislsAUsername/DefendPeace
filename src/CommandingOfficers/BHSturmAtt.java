package CommandingOfficers;

import java.util.Collection;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.PerfectMoveModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.UnitModel;

public class BHSturmAtt extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Sturm\nAttack", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BHSturmAtt();
    }
  }

  public BHSturmAtt()
  {
    super(coInfo);

    // legacy code left in for ~reasons~
//    PerfectMoveModifier moveMod = new PerfectMoveModifier();
//    Collection<UnitModel> perfects = moveMod.init(this).values();
//
//    unitModels.clear();
//    unitModels.addAll(perfects);

    // we need to mess with our shopping list as well, since we've replaced all our unit models
//    unitProductionByTerrain.get(TerrainType.FACTORY).clear();
//    unitProductionByTerrain.get(TerrainType.SEAPORT).clear();
//    unitProductionByTerrain.get(TerrainType.AIRPORT).clear();
//    for( UnitModel um : unitModels )
//    {
//      switch (um.chassis)
//      {
//        case AIR_HIGH:
//        case AIR_LOW:
//          unitProductionByTerrain.get(TerrainType.AIRPORT).add(um);
//          break;
//        case SHIP:
//        case SUBMERGED:
//          unitProductionByTerrain.get(TerrainType.SEAPORT).add(um);
//          break;
//        case TANK:
//        case TROOP:
//          unitProductionByTerrain.get(TerrainType.FACTORY).add(um);
//          break;
//      }
//    }
    for( UnitModel um : unitModels )
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        if( um.propulsion.getMoveCost(Weathers.CLEAR, terrain) < 99 )
        {
          um.propulsion.setMoveCost(Weathers.CLEAR, terrain, 1);
          um.propulsion.setMoveCost(Weathers.RAIN, terrain, 1);
        }
      }
    }

    new CODamageModifier(30).apply(this);
    new CODefenseModifier(-20).apply(this);

    addCommanderAbility(new MeteorStrike(this));
    addCommanderAbility(new MeatierStrike(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class MeteorStrike extends CommanderAbility
  {
    private static final String NAME = "Meteor Strike";
    private static final int COST = 6;
    private static final int POWER = 4;

    MeteorStrike(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      MassStrikeUtils.damageStrike(gameMap, POWER,
          MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true)), 2);
    }
  }

  private static class MeatierStrike extends CommanderAbility
  {
    private static final String NAME = "Meatier Strike";
    private static final int COST = 10;
    private static final int POWER = 8;

    MeatierStrike(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      MassStrikeUtils.damageStrike(gameMap, POWER,
          MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true)), 2);
    }
  }
}
