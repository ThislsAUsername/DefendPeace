package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.UnitModel;

public class BHSturm extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Sturm");
      infoPages.add(new InfoPage(
          "Sturm\r\n" + 
          "  Terrain cost is 1 on all terrain (except in snow). Units lose -20% attack, but gain +20% defense\r\n" + 
          "Meteor Strike -- A 2 Range missile hits the accumulation of the opponent's most expensive units and deals 4 HP damage\r\n" + 
          "Meteor Strike II -- A 2 Range missile hits the accumulation of the opponent's most expensive units and deals 8 HP damage"));
    }
    @Override
    public Commander create()
    {
      return new BHSturm();
    }
  }

  public BHSturm()
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
    for( UnitModel um : unitModels.values() )
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

    new CODamageModifier(-20).applyChanges(this);
    new CODefenseModifier(20).applyChanges(this);

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

