package CommandingOfficers.DefendPeace.misc;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.GamePath;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.UnitMods.UnitFightStatModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import UI.UIUtils;
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
      super("Qis", UIUtils.SourceGames.VARIANTS, UIUtils.MISC);
      infoPages.add(new InfoPage(
          "Commander Qis (pronounced 'keese') has spent his adult life fleeing cold weather, and failing to escape it.\n" +
          "Whether the cause is simply incredible misfortune or some sort of curse is unclear... he is quite curt on the subject.\n" +
          "What is known is that Qis has become incredibly adept at using the ever-present snow to his advantage."));
      infoPages.add(new InfoPage(
          "Passive:\r\n" +
          "Non-naval vehicles lose 1 movement when starting in rain or smoke (DoR/DS rain).\n" +
          "All units have perfect movement in snow.\n" +
          "The path each unit takes on your turn is coated with snow until your next turn."));
      infoPages.add(new InfoPage(
          GroovinMovin.NAME+" ("+GroovinMovin.COST+"):\n" +
          "+"+GroovinMovin.BUFF+" move for all units\n" +
          "Snow trails left this turn last +1 turn for each space moved so far" +
          "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
          TrailOfChill.NAME+" ("+TrailOfChill.COST+"):\n" +
          "+"+TrailOfChill.BUFF+" move for all units\n" +
          "All snow on the map will last "+TrailOfChill.DURATION+" turns" +
          "+10 attack and defense.\n"));
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new GroovinMovin(this, cb));
    addCommanderAbility(new TrailOfChill(this, cb));
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    // Perfect movement on snow
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      final int moveCost = uc.moveType.getMoveCost(Weathers.SNOW, terrain);
      if( MoveType.IMPASSABLE > moveCost && moveCost > 1 )
        uc.moveType.setMoveCost(Weathers.SNOW, terrain, 1);
    }
  }

  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.map == null || !uc.map.isLocationValid(uc.coord) )
      return;
    if( uc.model.isSeaUnit() || uc.model.isTroop() )
      return;

    // Qis's vehicles hate being wet.
    Weathers weatherType = uc.map.getEnvironment(uc.coord).weatherType;
    if( weatherType == Weathers.RAIN ||
        weatherType == Weathers.SMOKE )
      uc.movePower -= 1;
  }

  @Override
  protected void onTurnInit(MapMaster map, GameEventQueue events)
  {
    snowTrailTurns = 1;
    snowTrailShouldScale = false;
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
    for( XYCoord coord : path.getWaypoints() )
    {
      Environment env = Environment.getTile(army.myView.getEnvironment(coord).terrainType, Weathers.SNOW);
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

    NyoomAbility(Qis commander, String name, int cost, int buff, CostBasis basis)
    {
      super(commander, name, cost, basis);
      moveMod = new UnitMovementModifier(buff);
      coCast = commander;
      AIFlags = PHASE_TURN_START;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
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

    GroovinMovin(Qis commander, CostBasis basis)
    {
      super(commander, NAME, COST, BUFF, basis);
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
    private static final int BUFF = 3;
    private static final int DURATION = 3;

    TrailOfChill(Qis commander, CostBasis basis)
    {
      super(commander, NAME, COST, BUFF, basis);
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
