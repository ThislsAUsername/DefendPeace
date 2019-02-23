package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Stack;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 * Commander Ave (AH-vey) loves the cold, and her power allows her to inexorably, if slowly,
 * grind her opponents down beneath a wall of ever-encroaching ice.
 * Despite pernicious rumors to the contrary, she has nothing against Christmas.
 * 
 * Passive:
 *    Ave generates snow around all owned properties, which spreads over time.
 *    Her units take a movement and defense penalty in forests.
 * 
 * Glacio:
 *    Boosts the snow-aura around her buildings;
 *    Hail falls in a two-space radius around her units,
 *      changing the weather in those tiles to snow,
 *      damaging enemies for up to 2HP, and
 *      destroying any forests (reducing them to grass).
 *
 * Likes: Sleigh rides, Hot Chocolate
 * Dislikes: Jungles, Large Cats
 */
public class CommanderAve extends Commander
{
  public static final int SNOW_THRESHOLD = 100; // Big numbers for integer math.
  public static final int SNOW_PER_TURN = 400;
  public static final int SNOW_MELT_RATE = 100;
  private int MAX_SNOW_SPREAD_RANGE = 5;
  private CitySnowifier snowifier;

  private static final CommanderInfo coInfo = new CommanderInfo("Ave", new instantiator());  
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new CommanderAve();
    }
  }

  private int[][] snowMap;

  public CommanderAve()
  {
    super(coInfo);

    // Ave's units are fine in the snow, but not in the trees.
    for( UnitModel um : unitModels )
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        um.propulsion.setMoveCost(Weathers.SNOW, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
      }
      for( Weathers weather : Weathers.values() )
      {
        um.propulsion.setMoveCost(weather, TerrainType.FOREST, um.propulsion.getMoveCost(weather, TerrainType.FOREST)+1);
      }
    }

    addCommanderAbility(new GlacioAbility(this));
    snowifier = new CitySnowifier(this);
    GameEventListener.registerEventListener(snowifier);
  }

  @Override
  public GameEventQueue initTurn(GameMap gameMap)
  {
    GameEventQueue returnEvents = super.initTurn(gameMap);

    // Initialize our snow tracker if needed.
    if( null == snowMap )
    {
      System.out.println("First time init: adding one snow");
      // Start by initializing owned properties to SNOW_THRESHOLD
      snowMap = new int[gameMap.mapWidth][gameMap.mapHeight];
      addSnow(SNOW_THRESHOLD, gameMap, returnEvents);
    }

    // Add snow to all buildings we own.
    addSnow(SNOW_PER_TURN, gameMap, returnEvents); // Enough to spread a bit each way.
    relevelSnow(gameMap, returnEvents); // Enough to spread a bit each way.

    if(snowLoggingEnabled) log("[Commander Ave.initTurn] Snow Map: \n" + getSnowMapAsString());
    return returnEvents;
  }

  /** Ave's units take less cover from forests. */
  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( params.defender.CO == this // We are defending, in a FOREST
        && (params.combatRef.gameMap.getEnvironment(params.defender.x, params.defender.y).terrainType == TerrainType.FOREST)
        && (params.defender.model.chassis != UnitModel.ChassisEnum.AIR_HIGH)  // And our unit is actually on the ground.
        && (params.defender.model.chassis != UnitModel.ChassisEnum.AIR_LOW))
    {
      params.terrainDefense--;
    }
  }

  private boolean snowLoggingEnabled = false;
  public void log(boolean enable)
  {
    snowLoggingEnabled = enable;
  }
  private void log(String message)
  {
    if(snowLoggingEnabled) System.out.println(message);
  }

  /**
   * Dump extra snow on Ave's properties and then spread it around.
   * outEvents will be populated with any MapChangeEvents that result.
   */
  private void addSnow(double amount, GameMap gameMap, GameEventQueue outEvents)
  {
    log("============ Adding snow ========================================");
    ArrayList<MapChangeEvent.EnvironmentAssignment> tiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    for( XYCoord xyc : ownedProperties )
    {
      // Boost the amount of snow power this property has.
      snowMap[xyc.xCoord][xyc.yCoord] += amount;
      if( gameMap.getEnvironment(xyc).weatherType != Weathers.SNOW )
      {
        Environment envi = Environment.getTile(gameMap.getEnvironment(xyc).terrainType, Weathers.SNOW);
        tiles.add(new MapChangeEvent.EnvironmentAssignment(xyc, envi, 2));
      }
    }
    if( !tiles.isEmpty())
    {
      GameEvent event = new MapChangeEvent(tiles);
      outEvents.add(event);
    }
  }

  /**
   * Spread snow around.
   * outEvents will be populated with any MapChangeEvents that result.
   */
  private void relevelSnow(GameMap gameMap, GameEventQueue outEvents)
  {
    log("  Starting snow map:");
    log(getSnowMapAsString());

    HashSet<XYCoord> roots = new HashSet<XYCoord>();
    HashSet<XYCoord> frontier = new HashSet<XYCoord>(); // The next set of tiles to expand.
    HashSet<XYCoord> disconnected = new HashSet<XYCoord>(); // Tiles with snow, but that haven't been expanded.
    HashSet<XYCoord> toSnow = new HashSet<XYCoord>(); // Tiles to have their snow durations updated

    // Initialize our problem space.
    // We start by expanding the faucets/owned props. All other snow tiles go to unused for now.
    for( XYCoord prop : ownedProperties )
    {
      //sortedLeaves.add(new SnowPail(prop, snowMap[prop.xCoord][prop.yCoord]));
      frontier.add(prop);
    }
    // Label all snow tiles as disconnected to begin, and relabel as needed.
    for( int y = 0; y < gameMap.mapHeight; ++y )
    {
      for( int x = 0; x < gameMap.mapWidth; ++x )
      {
        if( snowMap[x][y] > 0 )
        {
          XYCoord coord = new XYCoord(x, y);
          if( !ownedProperties.contains(coord) )
            disconnected.add(coord);
        }
      }
    }

    int spreadIteration = 0;
    while( !frontier.isEmpty() && spreadIteration < MAX_SNOW_SPREAD_RANGE )
    {
      spreadIteration++;

      // Keep a reference so we remember where the snow wants to go.
      // Within the while loop below, we should always read from oldSnowMap and write to snowMap.
      int[][] oldSnowMap = getSnowMapClone();

      // Prepare the next round of snow expansion.
      PriorityQueue<SnowPail> sortedLeaves = new PriorityQueue<SnowPail>(); // The current set of tiles to expand.
      sortedLeaves.clear();
      for( XYCoord front : frontier )
      {
        sortedLeaves.add(new SnowPail(front, oldSnowMap[front.xCoord][front.yCoord]));
      }
      frontier.clear();

      // We want to process the current batch of leaves deepest-first, so unload our queue (which
      // is shallowest-first) into a stack and use that instead.
      Stack<XYCoord> leafStack = new Stack<XYCoord>();
      while(!sortedLeaves.isEmpty()) leafStack.push(sortedLeaves.poll().snowCoord);
      while(!leafStack.isEmpty())
      {
        Iterator<XYCoord> stackIter = leafStack.iterator();
        StringBuffer stackBuf = new StringBuffer("current stack: ");
        while( stackIter.hasNext() ) stackBuf.append(stackIter.next()).append(" ");
        log(stackBuf.toString());

        // Get the leaf; only noted with at least SNOW_THRESHOLD are able to be expanded.
        XYCoord leaf = leafStack.pop();
        roots.add(leaf); // We are processing this node and don't want to revisit it.
        disconnected.remove(leaf); // Don't consider this tile for melting later.
        log(String.format("Tile %s is connected", leaf));

        if( oldSnowMap[leaf.xCoord][leaf.yCoord] <= SNOW_THRESHOLD )
        {
          log("  Skipping shallow leaf " + leaf);
          continue;
        }

        log(getSnowMapAsString());

        // Figure out how much snow we can spread from this leaf.
        int snowToSpread = oldSnowMap[leaf.xCoord][leaf.yCoord] - SNOW_THRESHOLD; // Have to leave some behind.
        snowMap[leaf.xCoord][leaf.yCoord] = SNOW_THRESHOLD;

        // Collect the adjacent tiles that can collect snow from leaf.
        XYCoord adjUp = new XYCoord(leaf.xCoord, leaf.yCoord-1);
        XYCoord adjDown = new XYCoord(leaf.xCoord, leaf.yCoord+1);
        XYCoord adjLeft = new XYCoord(leaf.xCoord-1, leaf.yCoord);
        XYCoord adjRight = new XYCoord(leaf.xCoord+1, leaf.yCoord);
        XYCoord[] potentials = {adjUp, adjDown, adjLeft, adjRight};

        log("Expanding leaf " + leaf);
        log("  snow to spread: " + snowToSpread);
        log("  adjacents:");

        // Sort valid neighbors by snow depth, lowest first.
        PriorityQueue<SnowPail> workingSet = new PriorityQueue<SnowPail>();
        workingSet.offer(new SnowPail(leaf, SNOW_THRESHOLD));
        for( XYCoord pot : potentials )
          if( gameMap.isLocationValid(pot) )
          {
            SnowPail neighbor = new SnowPail(pot, oldSnowMap[pot.xCoord][pot.yCoord]);
            workingSet.offer(neighbor);
            log("    " + pot + ": " + neighbor.snowDepth);
          }

        // This will hold the tiles we are spreading snow to.
        HashSet<XYCoord> shallowTiles = new HashSet<XYCoord>();
        while( (snowToSpread > 0) && (!workingSet.isEmpty()) )
        {
          log("Snow to spread: " + snowToSpread);
          // Pull out the most empty tiles still in the working set.
          double mostShallow = workingSet.peek().snowDepth;
          log(" most shallow: " + mostShallow);
          while( !workingSet.isEmpty() && (workingSet.peek().snowDepth == mostShallow) )
          {
            log(" adding " + workingSet.peek().snowCoord + " to working set");
            shallowTiles.add(workingSet.poll().snowCoord);
          }
          double nextMostShallow = (workingSet.isEmpty()) ? mostShallow+snowToSpread : workingSet.peek().snowDepth;
          log(" next most shallow: " + nextMostShallow);

          // Add snow to the minimum tiles in equal measure until they reach the next minimum depth.
          int numShallowTiles = shallowTiles.size(); // The number of equally-empty tiles.
          if( numShallowTiles > snowToSpread )
          {
            snowToSpread = 0; // Not enough left to matter.
            continue;
          }
          double difference = nextMostShallow - mostShallow;
          double debt = difference * numShallowTiles;
          double payment = 0;

          // Calculate how much to add to each of the most shallow tiles in our working set.
          if( snowToSpread > debt ) // We can bring all the shallow tiles up to the next level.
            payment = difference;
          else // Just add as much as we can.
            payment = snowToSpread / numShallowTiles;

          // Make the donation.
          for( XYCoord coord : shallowTiles )
          {
            log("    moving " + payment + " snow to " + coord);
            snowToSpread -= payment;
            snowMap[coord.xCoord][coord.yCoord] += payment;
            if( (snowMap[coord.xCoord][coord.yCoord] >= SNOW_THRESHOLD) )
            {
              toSnow.add(coord);
              log(String.format("Setting environment of %s to SNOW", coord));
            }

            // Add coord to the frontier if it's not already in the current set of leaves so we can expand it next.
            if( !roots.contains(coord) && !leafStack.contains(coord) ) // If we haven't seen this next node yet, add it
            {
              log(String.format("Adding %s to frontier", coord));
              frontier.add(coord);  // to the frontier so we can process it on the next pass.
            }
          }
        } // while snowToSpread > 0
      } // !while( !leafIter.hasNext() )

      log(getSnowMapAsString());
      log("---------- FINISHED current set of leaves -------------------" );
    } // ~while( !leaves.isEmpty() )

    // If we run out of iterations, make sure we record that the
    // last set of "next tiles" is still connected.
    for( XYCoord front : frontier )
    {
      disconnected.remove(front);
    }

    // Any tiles that are no longer being fed snow will melt over time.
    for(XYCoord dis : disconnected )
    {
      int oldVal = snowMap[dis.xCoord][dis.yCoord];
      snowMap[dis.xCoord][dis.yCoord] = (oldVal - SNOW_MELT_RATE < 0)? 0 : oldVal - SNOW_MELT_RATE;
      if( snowLoggingEnabled ) log("Snow at " + dis + " melting from " + oldVal + " to " + snowMap[dis.xCoord][dis.yCoord]);
    }

    // Update weather forecast.
    ArrayList<MapChangeEvent.EnvironmentAssignment> tiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    for (XYCoord coord : toSnow)
    {
      Environment newEnvi = Environment.getTile(gameMap.getEnvironment(coord).terrainType, Weathers.SNOW);
      int duration = snowMap[coord.xCoord][coord.yCoord] / SNOW_MELT_RATE;
      if( coord.xCoord == 8 && coord.yCoord == 0 )
        System.out.println(String.format("tile %s has depth %s; setting duration to %s", coord, snowMap[coord.xCoord][coord.yCoord], duration));
      tiles.add(new MapChangeEvent.EnvironmentAssignment(coord, newEnvi, duration));
    }
    if( !tiles.isEmpty())
    {
      GameEvent event = new MapChangeEvent(tiles);
      outEvents.add(event);
    }
  }

  public int[][] getSnowMapClone()
  {
    int xsize = snowMap.length;
    int ysize = snowMap[0].length;
    int[][] snowClone = new int[xsize][ysize];
    for( int y = 0; y < ysize; ++y )
    {
      for( int x = 0; x < xsize; ++x )
      {
        snowClone[x][y] = snowMap[x][y];
      }
    }
    return snowClone;
  }

  public String getSnowMapAsString()
  {
    StringBuffer buffer = new StringBuffer();
    for( int y = 0; y < snowMap[0].length; ++y )
    {
      for( int x = 0; x < snowMap.length; ++x )
      {
        buffer.append(String.format("%5d", snowMap[x][y])).append(' ');
      }
      buffer.append('\n');
    }
    return buffer.toString();
  }

  /**
   *  Boosts the snow-aura around her buildings, and allows it to (briefly) extend further than normal.
   *  Deposits snow in a two-space around all of her units.
   *  Enemies within this radius are damaged for up to 2HP.
   *  Forests within this radius are destroyed (turned to grass).
   */
  private static class GlacioAbility extends CommanderAbility
  {
    private static final String GLACIO_NAME = "Glacio";
    private static final int GLACIO_COST = 15;
    private static final int GLACIO_BUFF = 10; // Standard 10
    private static final int GLACIO_MAX_SNOW_SPREAD_RANGE = 10;
    private static final int GLACIO_SNOW_MULTIPLIER = 4;

    CommanderAve Ave;
    COModifier damageMod = null;
    COModifier snowSpreadMod = null;

    GlacioAbility(CommanderAve commander)
    {
      super(commander, GLACIO_NAME, GLACIO_COST);
      Ave = commander;
      damageMod = new CODamageModifier(GLACIO_BUFF);
      snowSpreadMod = new SnowSpreadDistanceModifier(Ave, GLACIO_MAX_SNOW_SPREAD_RANGE);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Normal CO-power boost.
      myCommander.addCOModifier(damageMod);
      myCommander.addCOModifier(snowSpreadMod);

      // Keep track of any tiles that change to snow.
      GameEventQueue glacioEvents = new GameEventQueue();

      // Add extra snow to any tile that already has snow, plus extra on owned buildings.
      for( int x = 0; x < Ave.snowMap.length; ++x )
        for( int y = 0; y < Ave.snowMap[0].length; ++y )
        {
          Ave.snowMap[x][y] *= GLACIO_SNOW_MULTIPLIER;
        }
      Ave.addSnow(SNOW_PER_TURN*GLACIO_SNOW_MULTIPLIER, gameMap, glacioEvents);
      Ave.relevelSnow(gameMap, glacioEvents);

      // Change terrain to snow around each of Ave's units, and damage trees and enemies.
      HashSet<XYCoord> tilesSeen = new HashSet<XYCoord>();
      ArrayList<MapChangeEvent.EnvironmentAssignment> tileChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
      for( Unit unit : myCommander.units )
      {
        // Two-tile radius of effect around each unit.
        ArrayList<XYCoord> tiles = Utils.findLocationsInRange(gameMap, new XYCoord(unit.x, unit.y), 0, 2);
        for( XYCoord coord : tiles )
        {
          // Don't Glacio the same tile twice.
          if( tilesSeen.contains(coord) ) continue;
          tilesSeen.add(coord);

          // Add snow around friendlies.
          Location loc = gameMap.getLocation(coord);

          // Turn the spaces around each unit to snow.
          Environment tileEnvi = loc.getEnvironment();
          if((tileEnvi.weatherType != Weathers.SNOW) || (tileEnvi.terrainType == TerrainType.FOREST))
          {
            // Destroy any forests. Big hail, man.
            TerrainType newTerrain = (loc.getEnvironment().terrainType == TerrainType.FOREST)
                ? TerrainType.GRASS : loc.getEnvironment().terrainType;
            tileChanges.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(newTerrain, Weathers.SNOW), 1));
          }

          // Damage each enemy nearby.
          if( null != loc.getResident() && myCommander.isEnemy(loc.getResident().CO) )
          {
            loc.getResident().alterHP(-2);
          }
        }
      }
      glacioEvents.add(new MapChangeEvent(tileChanges));

      // Do all of our terrain alterations.
      while(!glacioEvents.isEmpty())
      {
        GameEvent event = glacioEvents.poll();
        event.performEvent(gameMap);
        GameEventListener.publishEvent(event);
      }
    }
  } // ~Glacio

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class SnowPail implements Comparable<SnowPail>
  {
    public final XYCoord snowCoord;
    public final int snowDepth;

    public SnowPail(XYCoord coord, int snowAmt)
    {
      snowCoord = coord;
      snowDepth = snowAmt; // Truncate to two-digit precision.
    }

    @Override
    public int compareTo(SnowPail other)
    {
      // Sort them shallowest first.
      return snowDepth - other.snowDepth;
    }
  }

  private static class SnowSpreadDistanceModifier implements COModifier
  {
    CommanderAve Ave;
    private int newDistance;
    private int oldDistance;

    public SnowSpreadDistanceModifier(CommanderAve cmdr, int newDist)
    {
      Ave = cmdr;
      oldDistance = Ave.MAX_SNOW_SPREAD_RANGE;
      newDistance = newDist;
    }

    @Override
    public void apply(Commander commander)
    {
      if( commander == Ave )
      {
        Ave.MAX_SNOW_SPREAD_RANGE = newDistance;
      }
    }

    @Override
    public void revert(Commander commander)
    {
      if( commander == Ave )
      {
        Ave.MAX_SNOW_SPREAD_RANGE = oldDistance;
      }
    }
  }

  private static class CitySnowifier extends GameEventListener
  {
    CommanderAve Ave;
    public CitySnowifier(CommanderAve cmdr)
    {
      Ave = cmdr;
    }
    @Override
    public void receiveCaptureEvent(Unit unit, Location location)
    {
      if( unit.CO == Ave && (location.getOwner() == Ave) )
      {
        // Just mark the tile as "snowy" until the next turnInit(), since we can't do a MapChangeEvent from here.
        XYCoord where = location.getCoordinates();
        Ave.snowMap[where.xCoord][where.yCoord] += SNOW_THRESHOLD;
      }
    }
  }
}
