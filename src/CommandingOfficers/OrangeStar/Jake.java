package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.IndirectRangeBoostModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Units.UnitModel;

public class Jake extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Jake");
      infoPages.add(new InfoPage(
          "Jake\r\n" + 
          "  Units (even aircraft) gain +10% attack power on plains\r\n" + 
          "Beat Down -- Land indirects gain +1 Range; Plains bonus increased by +10%\r\n" + 
          "Block Rock -- Land indirects gain +1 Range; Plains bonus increased by +30%; Vehicles gain +2 Movement\r\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Jake(rules);
    }
  }
  
  private int plainsBuff = 10;

  public Jake(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new BeatDown(this));
    addCommanderAbility(new BlockRock(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.plainsBuff = 10;
    return super.initTurn(map);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    Location loc = params.map.getLocation(params.attacker.x, params.attacker.y);
    if( loc != null && loc.getEnvironment().terrainType == TerrainType.GRASS )
    {
      params.attackPower += plainsBuff;
    }
  }

  private static class BeatDown extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Beat Down";
    private static final int COST = 3;
    private static final int VALUE = 10;
    Jake COcast;

    BeatDown(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Jake) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.plainsBuff += VALUE;

      IndirectRangeBoostModifier rangeMod = new IndirectRangeBoostModifier(1);
      for( UnitModel um : COcast.unitModels )
      {
        if( um.isAny(UnitModel.TANK) )
        {
          rangeMod.addApplicableUnitModel(um);
        }
      }
      COcast.addCOModifier(rangeMod);
    }
  }

  private static class BlockRock extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Block Rock";
    private static final int COST = 6;
    private static final int VALUE = 30;
    Jake COcast;

    BlockRock(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Jake) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.plainsBuff += VALUE;

      COMovementModifier moveMod = new COMovementModifier(2);
      IndirectRangeBoostModifier rangeMod = new IndirectRangeBoostModifier(1);

      for( UnitModel um : COcast.unitModels )
      {
        if( um.isAny(UnitModel.TANK) )
        {
          moveMod.addApplicableUnitModel(um);
          rangeMod.addApplicableUnitModel(um);
        }
      }

      COcast.addCOModifier(rangeMod);
      myCommander.addCOModifier(moveMod);
    }
  }
}

