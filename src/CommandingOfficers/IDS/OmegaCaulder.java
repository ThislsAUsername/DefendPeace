package CommandingOfficers.IDS;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;

import java.util.ArrayList;
import java.util.Collections;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Engine.GameEvents.MoveEvent;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;

public class OmegaCaulder extends TabithaEngine
{
  private static final long serialVersionUID = 1L;
  public static final int MEGA_ATK = 70;
  public static final int MEGA_DEF = 45;
  public static final int D2DREPAIRS = 5;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Omega Caulder");
      infoPages.add(new InfoPage(
            "Called \'Omega\' because he's extra fair and balanced.\n"
          + "After his first move, automatically grants up to 3 \'Mega Boost\'s of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"
          + "All damaged units are repaired for +"+ D2DREPAIRS +" HP every turn (liable for costs).\r\n"
          + "Unaffected by weather."));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
            "APOCALYPSE (13):\n"
          + "I heard you like pain?"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new OmegaCaulder(rules);
    }
  }

  @Override
  public int getMegaBoostCount() {return 3;}

  public OmegaCaulder(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);

    for( UnitModel um : unitModels )
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        um.propulsion.setMoveCost(Weathers.RAIN, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
        um.propulsion.setMoveCost(Weathers.SNOW, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
        um.propulsion.setMoveCost(Weathers.SANDSTORM, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
      }
    }

    addCommanderAbility(new APOCALYPSE(this));
  }

  protected static class APOCALYPSE extends NukeIt
  {
    private static final long serialVersionUID = 1L;

    APOCALYPSE(TabithaEngine commander)
    {
      super(commander, "APOCALYPSE", 13, 8, 50, 35);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
    }
    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue abilityEvents = new GameEventQueue();

      GameEvent event = new GlobalWeatherEvent(Weathers.SNOW, 3);
      abilityEvents.add(event);

      return abilityEvents;
    }
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    GameEventQueue ret = super.initTurn(map);

    for( Unit unit : units )
    {
      int costPerHP = unit.model.getCost() / unit.model.maxHP;

      int affordableHP = this.money / costPerHP;
      int actualRepair = Math.min(D2DREPAIRS, affordableHP);

      int deltaHP = unit.alterHP(actualRepair);
      this.money -= deltaHP * costPerHP;
    }

    return ret;
  }

  public void receiveMoveEvent(MoveEvent event)
  {
    super.receiveMoveEvent(event);
    if( this != event.unit.CO )
      return; // I can only boost during my own turn

    ArrayList<XYCoord> unitCoords = new ArrayList<XYCoord>();
    for( Unit unit : this.units )
    {
      XYCoord coord = new XYCoord(unit.x, unit.y);
      if( unit.getHP() > 5 && myView.isLocationValid(coord) )
        unitCoords.add(coord);
    }
    // Find the units farthest from my own base
    Utils.sortLocationsByDistance(HQLocation, unitCoords);
    Collections.reverse(unitCoords);

    for( XYCoord coord : unitCoords )
    {
      if( COUs.size() >= getMegaBoostCount() )
        break;
      Unit unit = myView.getLocation(coord).getResident();
      if( null != unit )
        COUs.add(unit);
    }
  };

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
