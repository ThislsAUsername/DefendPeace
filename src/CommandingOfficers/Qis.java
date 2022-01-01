package CommandingOfficers;

import java.util.ArrayList;
import Engine.GamePath;
import Engine.GamePath.PathNode;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;
import Units.MoveTypes.MoveType;

public class Qis extends Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Qis");
      infoPages.add(new InfoPage(
          "Commander Qis (pronounced 'keese') has spent his adult life fleeing cold weather, and failing to escape it.\n" +
          "Whether the cause is simply incredible misfortune or some sort of curse is unclear... he is quite curt on the subject.\n" +
          "What is known is that Qis has become incredibly adept at using the ever-present snow to his advantage."));
      infoPages.add(new InfoPage(
          "Passive:\r\n" +
          "Watery terrain slows your non-naval vehicles, and they lose -1 movement when starting in rain\n" +
          "All units have perfect movement in snow.\n" +
          "The path each unit takes on your turn is coated with snow until your next turn."));
      infoPages.add(new InfoPage(
          GroovinMovin.NAME+" ("+GroovinMovin.COST+"):\n" +
          "+"+GroovinMovin.BUFF+" move for all units\n" +
          "Snow trails left this turn last +1 turn for each space moved so far"));
      infoPages.add(new InfoPage(
          TrailOfChill.NAME+" ("+TrailOfChill.COST+"):\n" +
          "+"+TrailOfChill.BUFF+" move for all units\n" +
          "All snow on the map will last "+TrailOfChill.DURATION+" turns"));
      infoPages.add(new InfoPage(
          "Hit: Deserts, Desserts\n" +
          "Miss: Showing up first"));
      infoPages.add(new InfoPage(
          "Original art credit:\n" +
          "@Smile ❤#3047 Discord ID 374728627373211651"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Qis(rules);
    }
  }

  private int snowTrailTurns = 1;
  private boolean snowTrailShouldScale = false;

  public Qis(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new GroovinMovin(this));
    addCommanderAbility(new TrailOfChill(this));
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    // Perfect movement on snow
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      if( MoveType.IMPASSABLE > uc.moveType.getMoveCost(Weathers.SNOW, terrain) )
        uc.moveType.setMoveCost(Weathers.SNOW, terrain, 1);
    }

    if( uc.model.isSeaUnit() || uc.model.isTroop() )
      return;

    // Qis's vehicles hate being wet.
    for( Weathers weather : Weathers.values() )
    {
      if( weather == Weathers.SNOW )
        continue; // Snow is handled elsewhere

      // Watery terrain costs +1 move
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        if( terrain.isWater() || terrain == TerrainType.RIVER )
          uc.moveType.setMoveCost(weather, terrain, uc.moveType.getMoveCost(weather, terrain) + 1);
      }
    }
  }

  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.map == null || uc.coord == null )
      return;
    if( uc.model.isSeaUnit() || uc.model.isTroop() )
      return;

    // Qis's vehicles hate being wet.
    if( uc.map.getEnvironment(uc.coord).weatherType == Weathers.RAIN )
      uc.movePower -= 1;
  }

  @Override
  public GameEventQueue initTurn(MapMaster gameMap)
  {
    snowTrailTurns = 1;
    snowTrailShouldScale = false;

    GameEventQueue returnEvents = super.initTurn(gameMap);
    return returnEvents;
  }

  @Override
  public GameEventQueue receiveMoveEvent(Unit unit, GamePath path)
  {
    GameEventQueue returnEvents = new GameEventQueue();

    if( unit.CO != this )
      return returnEvents;
    // Drop snow all along the path
    ArrayList<MapChangeEvent.EnvironmentAssignment> snowTiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    int duration = snowTrailTurns;
    for( PathNode node : path.getWaypoints() )
    {
      XYCoord coord = node.GetCoordinates();
      Environment env = Environment.getTile(myView.getEnvironment(coord).terrainType, Weathers.SNOW);
      snowTiles.add(new MapChangeEvent.EnvironmentAssignment(coord, env, duration));
      if( snowTrailShouldScale )
        ++duration;
    }

    // Do all of our terrain alterations.
    returnEvents.add(new MapChangeEvent(snowTiles));
    return returnEvents;
  }

  private static class NyoomAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;

    public Qis coCast;
    UnitModifier statMod = new UnitFightStatModifier(10);
    UnitModifier moveMod;

    NyoomAbility(Qis commander, String name, int cost, int buff)
    {
      super(commander, name, cost);
      moveMod = new UnitMovementModifier(buff);
      coCast = commander;
      AIFlags = PHASE_TURN_START;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(moveMod);
    }
  }

  private static class GroovinMovin extends NyoomAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Groovin' Movin'";
    private static final int COST = 3;
    private static final int BUFF = 1;

    GroovinMovin(Qis commander)
    {
      super(commander, NAME, COST, BUFF);
      coCast = commander;
      AIFlags = PHASE_TURN_START;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      coCast.snowTrailShouldScale = true;
    }
  }

  private static class TrailOfChill extends NyoomAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Trail of Chill";
    private static final int COST = 5;
    private static final int BUFF = 2;
    private static final int DURATION = 3;

    TrailOfChill(Qis commander)
    {
      super(commander, NAME, COST, BUFF);
      coCast = commander;
      AIFlags = PHASE_TURN_START;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      coCast.snowTrailTurns = DURATION;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      // Set all pre-existing snow to last at least 3 turns
      ArrayList<MapChangeEvent.EnvironmentAssignment> snowTiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
      for( int x = 0; x < map.mapWidth; ++x )
      {
        for( int y = 0; y < map.mapHeight; ++y )
        {
          XYCoord coord = new XYCoord(x, y);
          Environment env = map.getEnvironment(x, y);
          if( env.weatherType == Weathers.SNOW )
          {
            snowTiles.add(new MapChangeEvent.EnvironmentAssignment(coord, env, DURATION));
          }
        }
      }

      // Do all of our terrain alterations.
      GameEventQueue events = new GameEventQueue();
      events.add(new MapChangeEvent(snowTiles));

      return events;
    }
  }

}
