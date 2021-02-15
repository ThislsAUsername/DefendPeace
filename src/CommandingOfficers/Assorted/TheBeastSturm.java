package CommandingOfficers.Assorted;

import Engine.GameScenario;
import Engine.XYCoord;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.DamagePopup;
import Engine.Combat.MassStrikeUtils;
import Terrain.GameMap;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.UnitModel;

public class TheBeastSturm extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("The Beast");
      infoPages.add(new InfoPage(
          "--THE BEAST--\r\n" + 
          "Terrain cost is 1 on all terrain (except in snow). Units gain +30% firepower, but lose -20% defense.\r\n" + 
          "SWARM, ROACHES!: A 2-range missile hits the accumulation of the opponent's most expensive units and deals 4 HP damage.\r\n" + 
          "GWAR HAR HAR!: A 2-range missile hits the accumulation of the opponent's most expensive units and deals 8 HP damage."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new TheBeastSturm(rules);
    }
  }

  public TheBeastSturm(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

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

    new CODamageModifier(30).applyChanges(this);
    new CODefenseModifier(-20).applyChanges(this);

    addCommanderAbility(new MeteorStrike(this));
    addCommanderAbility(new MeatierStrike(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class MeteorStrike extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Roach Swarm";
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
          findTarget(gameMap), 2);
    }
    private XYCoord findTarget(GameMap gameMap)
    {
      return MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true));
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      output.add(new DamagePopup(
                     findTarget(gameMap),
                     myCommander.myColor,
                     "Roaches"));

      return output;
    }
  }

  private static class MeatierStrike extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Gwar Har Har";
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
          findTarget(gameMap), 2);
    }
    private XYCoord findTarget(GameMap gameMap)
    {
      return MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true));
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      output.add(new DamagePopup(
                     findTarget(gameMap),
                     myCommander.myColor,
                     "GWAR HAR HAR"));

      return output;
    }
  }
}

