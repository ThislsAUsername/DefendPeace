package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Stack;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.MassDamageEvent;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 * Commander Ave (AH-vey) supports her skiing habit by slowly growing a mountain of fresh
 * snow around each of her buildings. This also allows her to gradually but inexorably
 * grind her opponents down beneath a wall of ever-encroaching ice.
 *
 * Passive:
 *    Ave generates snow around all owned properties, which spreads over time. The radius
 *    of effect is small at first, but can be expanded by her abilities.
 *    Her units move normally in snow, but take a movement and defense penalty in forests.
 *
 * Nix:
 *    Permanently expands the range of Ave's snow passive. This ability increases in cost
 *      more quickly than most other abilities as it is used.
 *    Ave's units gain a 10 percent increase in firepower.
 *
 * Glacio:
 *    Increases the snow-aura around her buildings by 3 spaces for the next turn.
 *    Snows on every tile in a 3-space radius around each of her units.
 *    Stuns any enemy unit within 2 spaces of one of Ave's units or buildings.
 *    Ave's units gain a 10 percent increase in firepower.
 *
 * Oblido:
 *    Hailstones rain down in a 2-space radius around Ave's units and buildings,
 *      damaging enemies for up to 2HP, and destroying any forests (reducing them to grass).
 *    Ave's units gain a 20-percent increase in firepower.
 *
 * Likes: Steep Slopes and Sharp Cuts
 * Dislikes: Trees and Mythical Snow Monsters
 */
public class Ave extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final int SNOW_THRESHOLD = 100; // Big numbers for integer math.
  public static final int SNOW_PER_TURN = 400;
  public static final int SNOW_MELT_RATE = 100;
  private int MAX_SNOW_SPREAD_RANGE = 2;
  private int MAX_SNOW_DEPTH = 500;
  private CitySnowifier snowifier;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Ave");
      infoPages.add(new InfoPage(
          "Commander Ave (AH-vey) supports her skiing habit by slowly growing a mountain of fresh " +
          "snow around each of her buildings. This also allows her to gradually but inexorably " +
          "grind her opponents down beneath a wall of ever-encroaching ice."));
      infoPages.add(new InfoPage(
          "Passive:\r\n" +
          "- Ave generates snow around all owned properties, which spreads over time.\n" +
          "- The radius of effect is small at first, but can be expanded by her abilities.\n" +
          "- Her units move normally in snow, but take a movement and defense penalty in forests."));
      infoPages.add(new InfoPage(
          "Nix ("+NixAbility.NIX_COST+"):\n" +
          "Ave's units gain a "+NixAbility.NIX_BUFF+"% increase in firepower.\n" +
          "Permanently expands the range of Ave's snow passive.\n" +
          "This ability increases in cost more quickly than most other abilities as it is used."));
      infoPages.add(new InfoPage(
          "Glacio ("+GlacioAbility.GLACIO_COST+"):\n" +
          "Ave's units gain a "+GlacioAbility.GLACIO_BUFF+"% increase in firepower.\n" +
          "Increases the snow-aura around her buildings by "+GlacioAbility.GLACIO_SNOW_SPREAD+" spaces for the next turn.\n" +
          "Snows on every tile in a "+GlacioAbility.GLACIO_SNOW_SPREAD+"-space radius around each of her units.\n" +
          "Stuns any enemy unit within "+GlacioAbility.GLACIO_FREEZE_RANGE+" spaces of one of Ave's units or buildings."));
      infoPages.add(new InfoPage(
          "Oblido ("+OblidoAbility.OBLIDO_COST+"):\n" +
          "Ave's units gain a "+OblidoAbility.OBLIDO_BUFF+"% increase in firepower.\n" +
          "Hailstones rain down in a "+OblidoAbility.OBLIDO_RANGE+"-space radius around Ave's units and buildings, damaging enemies for up to 2HP, and destroying any forests (reducing them to grass).\n"));
      infoPages.add(new InfoPage(
          "Likes: Steep Slopes and Sharp Cuts\n" +
          "Dislikes: Trees and Mythical Snow Monsters"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Ave(rules);
    }
  }

  private int[][] snowMap;

  public Ave(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    // Ave's units are fine in the snow, but not in the trees.
    for( UnitModel um : unitModels.values() )
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

    addCommanderAbility(new NixAbility(this));
    addCommanderAbility(new GlacioAbility(this));
    addCommanderAbility(new OblidoAbility(this));
    snowifier = new CitySnowifier(this);
    GameEventListener.registerEventListener(snowifier);
  }

  @Override
  public GameEventQueue initTurn(MapMaster gameMap)
  {
    GameEventQueue returnEvents = super.initTurn(gameMap);

    // Initialize our snow tracker if needed.
    if( null == snowMap )
    {
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
        && (params.defender.model.isLandUnit()))  // And our unit is actually on the ground.
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

        // Get the leaf; only nodes with at least SNOW_THRESHOLD are able to be expanded.
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
        ArrayList<XYCoord> potentials = Utils.findLocationsInRange(gameMap, leaf, 1);

        log("Expanding leaf " + leaf);
        log("  snow to spread: " + snowToSpread);
        log("  adjacents:");

        // Sort valid neighbors by snow depth, lowest first.
        PriorityQueue<SnowPail> workingSet = new PriorityQueue<SnowPail>();
        workingSet.offer(new SnowPail(leaf, SNOW_THRESHOLD));
        for( XYCoord pot : potentials )
          if( gameMap.isLocationValid(pot) && !roots.contains(pot) && !leafStack.contains(pot) )
          {
            SnowPail neighbor = new SnowPail(pot, oldSnowMap[pot.xCoord][pot.yCoord]);
            workingSet.offer(neighbor);
            log("    " + pot + ": " + neighbor.snowDepth);
          }

        // This will hold the tiles we are spreading snow to.
        HashSet<XYCoord> shallowTiles = new HashSet<XYCoord>();
        while( (snowToSpread > 0) )
        {
          log("Snow to spread: " + snowToSpread);
          // Pull out the most empty tiles still in the working set.
          double mostShallow = 0;
          double nextMostShallow = 0;
          if( !workingSet.isEmpty() )
          {
            mostShallow = workingSet.peek().snowDepth;
            while( !workingSet.isEmpty() && (workingSet.peek().snowDepth == mostShallow) )
            {
              log(" adding " + workingSet.peek().snowCoord + " to working set");
              shallowTiles.add(workingSet.poll().snowCoord);
            }
            nextMostShallow = (workingSet.isEmpty()) ? mostShallow+snowToSpread : workingSet.peek().snowDepth;
            if( nextMostShallow > MAX_SNOW_DEPTH ) nextMostShallow = MAX_SNOW_DEPTH; // Keep snow under the limit.
          }
          else
          {
            mostShallow = snowMap[leaf.xCoord][leaf.yCoord];
            if( mostShallow == MAX_SNOW_DEPTH )
              shallowTiles.remove(leaf); // If leaf is max depth, just push extra to other unprocessed tiles.
            nextMostShallow = mostShallow + snowToSpread;
          }
          log(" most shallow: " + mostShallow);
          log(" next most shallow: " + nextMostShallow);

          // Add snow to the minimum tiles in equal measure until they reach the next minimum depth.
          int numShallowTiles = shallowTiles.size(); // The number of equally-empty tiles.
          if( (numShallowTiles > snowToSpread) || (0 == numShallowTiles) )
          {
            snowToSpread = 0; // Not enough left to matter.
            continue;
          }

          // Find the amount of snow required to bring the shallowest tile in our current set up to the level of the next.
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
              log(String.format("Ensuring %s is SNOW", coord));
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
      if( snowMap[front.xCoord][front.yCoord] > MAX_SNOW_DEPTH )
        snowMap[front.xCoord][front.yCoord] = MAX_SNOW_DEPTH;
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
      tiles.add(new MapChangeEvent.EnvironmentAssignment(coord, newEnvi, duration));
    }
    if( !tiles.isEmpty())
    {
      GameEvent event = new MapChangeEvent(tiles);
      outEvents.add(event);
    }

    log("---------- FINAL state after relevel -------------------" );
    log(getSnowMapAsString());
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
   * Nix permanently increases the range of Ave's passive snow effect,
   * and converts all spaces within that range to snow.
   */
  private static class NixAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NIX_NAME = "Nix";
    private static final int NIX_COST = 1;
    private static final int NIX_BUFF = 10; // Standard 10

    private int numActivations = 0;

    Ave coCast;
    COModifier damageMod = null;

    NixAbility(Ave commander)
    {
      super(commander, NIX_NAME, NIX_COST);
      coCast = commander;
      damageMod = new CODamageModifier(NIX_BUFF);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void adjustCost()
    {
      // Override default cost-increase behavior to make this get more expensive faster.
      numActivations++;
      myPowerCost = 1 + (numActivations*3);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Increase Ave's sphere of influence.
      coCast.MAX_SNOW_SPREAD_RANGE++;

      // Buff units.
      coCast.addCOModifier(damageMod);

      // Drop snow everywhere inside her range.
      ArrayList<MapChangeEvent.EnvironmentAssignment> snowTiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
      HashSet<XYCoord> tiles = Utils.findLocationsNearProperties(gameMap, coCast, coCast.MAX_SNOW_SPREAD_RANGE);
      for( XYCoord coord : tiles )
      {
        if( coCast.snowMap[coord.xCoord][coord.yCoord] < Ave.SNOW_THRESHOLD )
        {
          coCast.snowMap[coord.xCoord][coord.yCoord] = Ave.SNOW_THRESHOLD;
          if( gameMap.getEnvironment(coord).weatherType != Weathers.SNOW )
          {
            snowTiles.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(gameMap.getEnvironment(coord).terrainType, Weathers.SNOW), 1));
          }
        }
      }

      // Do all of our terrain alterations.
      MapChangeEvent event = new MapChangeEvent(snowTiles);
      event.performEvent(gameMap);
      GameEventListener.publishEvent(event);
    }
  }

  /**
   *  Boosts the snow-aura around her buildings, and allows it to (briefly) extend further than normal.
   *  Deposits snow in a three-space range around all of her units.
   *  Enemies within 2 spaces of one of Ave's units or buildings are frozen for one turn.
   */
  private static class GlacioAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String GLACIO_NAME = "Glacio";
    private static final int GLACIO_COST = 8;
    private static final int GLACIO_BUFF = 10; // Standard 10
    private static final int GLACIO_SNOW_SPREAD = 3;
    private static final int GLACIO_FREEZE_RANGE = 2;

    Ave coCast;
    COModifier damageMod = null;

    GlacioAbility(Ave commander)
    {
      super(commander, GLACIO_NAME, GLACIO_COST);
      coCast = commander;
      damageMod = new CODamageModifier(GLACIO_BUFF);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Normal CO-power boost.
      myCommander.addCOModifier(damageMod);

      // Keep track of any tiles that change to snow.
      ArrayList<MapChangeEvent.EnvironmentAssignment> tileChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();

      // Add snow in an expanded range around Ave's areas.
      int maxSnowRange = coCast.MAX_SNOW_SPREAD_RANGE + GLACIO_SNOW_SPREAD;
      HashSet<XYCoord> tilesInRange = Utils.findLocationsNearProperties(gameMap, coCast, maxSnowRange);
      tilesInRange.addAll(Utils.findLocationsNearUnits(gameMap, coCast, GLACIO_SNOW_SPREAD));
      for( XYCoord coord : tilesInRange )
      {
        if( coCast.snowMap[coord.xCoord][coord.yCoord] < Ave.SNOW_THRESHOLD )
        {
          coCast.snowMap[coord.xCoord][coord.yCoord] = Ave.SNOW_THRESHOLD;
          if( gameMap.getEnvironment(coord).weatherType != Weathers.SNOW )
          {
            tileChanges.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(gameMap.getEnvironment(coord).terrainType, Weathers.SNOW), 1));
          }
        }
      }

      // Freeze enemies around each of Ave's units or buildings.
      tilesInRange = Utils.findLocationsNearUnits(gameMap, coCast, GLACIO_FREEZE_RANGE);
      tilesInRange.addAll(Utils.findLocationsNearProperties(gameMap, coCast, GLACIO_FREEZE_RANGE));
      for( XYCoord coord : tilesInRange )
      {
        // Freeze each nearby enemy.
        Location loc = gameMap.getLocation(coord);
        if( null != loc.getResident() && myCommander.isEnemy(loc.getResident().CO) )
        {
          loc.getResident().isStunned = true;
        }
      }

      GameEventQueue glacioEvents = new GameEventQueue();
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

  /**
   *  Destroys forests and does 2 HP of damage to all enemies within a 2-space radius around Ave's units and buildings.
   *  Enemies within this radius are damaged for up to 2HP.
   *  Forests within this radius are destroyed (turned to grass).
   *  This power scales in cost more slowly than average.
   */
  private static class OblidoAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String OBLIDO_NAME = "Oblido";
    private static final int OBLIDO_COST = 8;
    private static final int OBLIDO_BUFF = 20;
    private static final int OBLIDO_RANGE = 2;

    Ave Ave;
    COModifier damageMod = null;

    OblidoAbility(Ave commander)
    {
      super(commander, OBLIDO_NAME, OBLIDO_COST);
      Ave = commander;
      damageMod = new CODamageModifier(OBLIDO_BUFF);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void adjustCost()
    {
      // One of the big benefits of this power is deforestation, since trees get in Ave's way.
      // Trees are only removed once, so we'll increase cost more slowly to counteract the decreased utility.
      myPowerCost += 0.5;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Apply CO unit buffs.
      myCommander.addCOModifier(damageMod);

      // Keep track of any tiles that change.
      ArrayList<MapChangeEvent.EnvironmentAssignment> tileChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
      ArrayList<Unit> victims = new ArrayList<Unit>();

      // Change terrain to snow around each of Ave's units and buildings, and damage trees and enemies.
      HashSet<XYCoord> affectedTiles = Utils.findLocationsNearProperties(gameMap, Ave, OBLIDO_RANGE);
      affectedTiles.addAll(Utils.findLocationsNearUnits(gameMap, Ave, OBLIDO_RANGE));

      // Smash things. Don't add snow though.
      for( XYCoord coord : affectedTiles )
      {
        // Destroy any forests. Big hail, man.
        Location loc = gameMap.getLocation(coord);
        Environment tileEnvi = loc.getEnvironment();
        if(tileEnvi.terrainType == TerrainType.FOREST)
        {
          tileChanges.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(TerrainType.GRASS, loc.getEnvironment().weatherType), 1));
        }

        // Damage each enemy nearby.
        if( null != loc.getResident() && myCommander.isEnemy(loc.getResident().CO) )
        {
          victims.add(loc.getResident());
        }
      }

      GameEvent damage = new MassDamageEvent(victims, 2, false);
      damage.performEvent(gameMap);
      GameEventListener.publishEvent(damage);

      GameEvent tileChange = new MapChangeEvent(tileChanges);
      tileChange.performEvent(gameMap);
      GameEventListener.publishEvent(tileChange);
    }
  } // Oblido

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
      snowDepth = snowAmt;
    }

    @Override
    public int compareTo(SnowPail other)
    {
      // Sort them shallowest first.
      return snowDepth - other.snowDepth;
    }
  }

  private static class CitySnowifier extends GameEventListener
  {
    private static final long serialVersionUID = 1L;
    Ave Ave;
    public CitySnowifier(Ave cmdr)
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
