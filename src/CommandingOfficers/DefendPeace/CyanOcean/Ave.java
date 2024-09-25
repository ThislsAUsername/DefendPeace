package CommandingOfficers.DefendPeace.CyanOcean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.GameInstance;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MapChangeEvent;
import Engine.GameEvents.MassDamageEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import UI.UIUtils;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;

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
      super("Ave", UIUtils.SourceGames.DEFEND_PEACE, UIUtils.CO);
      infoPages.add(new InfoPage(
          "Commander Ave (AH-vey) supports her skiing habit by slowly growing a mountain of fresh " +
          "snow around each of her buildings. This allows her to gradually but inexorably " +
          "grind her opponents down beneath a wall of ever-encroaching ice."));
      infoPages.add(new InfoPage(
          "Passive:\r\n" +
          "Ave generates snow around all owned properties, which spreads over time.\n" +
          "The radius of effect is small at first, but can be expanded by her abilities.\n" +
          "Her units move normally in snow, but take a movement and defense penalty in forests."));
      infoPages.add(new InfoPage(
          "Nix ("+NixAbility.NIX_COST+"):\n" +
          "+"+NixAbility.NIX_BUFF+"% attack for all units\n" +
          "+1 Range for Ave's snow-aura passive\n" +
          "\nNOTE: This ability increases in cost more quickly than normal"));
      infoPages.add(new InfoPage(
          "Glacio ("+GlacioAbility.GLACIO_COST+"):\n" +
          "+"+GlacioAbility.GLACIO_BUFF+"% attack for all units\n" +
          "Expands the snow-aura around her buildings by "+GlacioAbility.GLACIO_SNOW_SPREAD+" spaces\n" +
          "Snows on every tile in a "+GlacioAbility.GLACIO_SNOW_SPREAD+"-space radius around each of her units\n" +
          "Stuns any enemy unit within "+GlacioAbility.GLACIO_FREEZE_RANGE+" spaces of one of Ave's units or buildings"));
      infoPages.add(new InfoPage(
          "Oblido ("+OblidoAbility.OBLIDO_COST+"):\n" +
          "+"+OblidoAbility.OBLIDO_BUFF+"% attack for all units\n" +
          "Hailstones fall in a "+OblidoAbility.OBLIDO_RANGE+"-space radius around Ave's units and buildings, damaging enemies for up to 2HP, and destroying any forests (reducing them to grass)\n"));
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

    // Ave's abilities scale separately since they each have a very different way they want to scale
    addCommanderAbility(new NixAbility(this));
    addCommanderAbility(new GlacioAbility(this));
    addCommanderAbility(new OblidoAbility(this));
  }

  @Override
  public void modifyMoveType(UnitContext uc)
  {
    // Ave's units are fine in the snow, but not in the trees.
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      uc.moveType.setMoveCost(Weathers.SNOW, terrain, uc.moveType.getMoveCost(Weathers.CLEAR, terrain));
    }
    for( Weathers weather : Weathers.values() )
    {
      uc.moveType.setMoveCost(weather, TerrainType.FOREST, uc.moveType.getMoveCost(weather, TerrainType.FOREST)+1);
    }
  }


  @Override
  public void initForGame(GameInstance game)
  {
    super.initForGame(game);
    snowifier = new CitySnowifier(this);
    snowifier.registerForEvents(game);
  }
  @Override
  public void deInitForGame(GameInstance game)
  {
    super.deInitForGame(game);
    snowifier.unregister(game);
  }

  @Override
  protected void onTurnInit(MapMaster gameMap, GameEventQueue returnEvents)
  {
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
  }

  /** Ave's units take less cover from forests. */
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.env == null )
      return;
    // We are defending, in a FOREST
    if( (params.defender.env.terrainType == TerrainType.FOREST)
        && (params.defender.model.isLandUnit()))  // And our unit is actually on the ground.
    {
      params.terrainStars--;
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
      snowMap[xyc.x][xyc.y] += amount;
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
        sortedLeaves.add(new SnowPail(front, oldSnowMap[front.x][front.y]));
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

        if( oldSnowMap[leaf.x][leaf.y] <= SNOW_THRESHOLD )
        {
          log("  Skipping shallow leaf " + leaf);
          continue;
        }

        log(getSnowMapAsString());

        // Figure out how much snow we can spread from this leaf.
        int snowToSpread = oldSnowMap[leaf.x][leaf.y] - SNOW_THRESHOLD; // Have to leave some behind.
        snowMap[leaf.x][leaf.y] = SNOW_THRESHOLD;

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
            SnowPail neighbor = new SnowPail(pot, oldSnowMap[pot.x][pot.y]);
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
            mostShallow = snowMap[leaf.x][leaf.y];
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
            snowMap[coord.x][coord.y] += payment;
            if( (snowMap[coord.x][coord.y] >= SNOW_THRESHOLD) )
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
      if( snowMap[front.x][front.y] > MAX_SNOW_DEPTH )
        snowMap[front.x][front.y] = MAX_SNOW_DEPTH;
      disconnected.remove(front);
    }

    // Any tiles that are no longer being fed snow will melt over time.
    for(XYCoord dis : disconnected )
    {
      int oldVal = snowMap[dis.x][dis.y];
      snowMap[dis.x][dis.y] = (oldVal - SNOW_MELT_RATE < 0)? 0 : oldVal - SNOW_MELT_RATE;
      if( snowLoggingEnabled ) log("Snow at " + dis + " melting from " + oldVal + " to " + snowMap[dis.x][dis.y]);
    }

    // Update weather forecast.
    ArrayList<MapChangeEvent.EnvironmentAssignment> tiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
    for (XYCoord coord : toSnow)
    {
      Environment newEnvi = Environment.getTile(gameMap.getEnvironment(coord).terrainType, Weathers.SNOW);
      int duration = snowMap[coord.x][coord.y] / SNOW_MELT_RATE;
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

    Ave coCast;
    UnitModifier damageMod = null;

    NixAbility(Ave commander)
    {
      super(commander, NIX_NAME, NIX_COST);
      coCast = commander;
      damageMod = new UnitDamageModifier(NIX_BUFF);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public int getCost()
    {
      // Override default cost-increase behavior to make this get more expensive faster.
      int myStars = 1 + (costBasis.numCasts*3);
      return myStars * CHARGERATIO_FUNDS;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(damageMod);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Increase Ave's sphere of influence.
      coCast.MAX_SNOW_SPREAD_RANGE++;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Drop snow everywhere inside her range.
      ArrayList<MapChangeEvent.EnvironmentAssignment> snowTiles = new ArrayList<MapChangeEvent.EnvironmentAssignment>();
      Set<XYCoord> tiles = Utils.findLocationsNearProperties(gameMap, coCast, coCast.MAX_SNOW_SPREAD_RANGE);
      for( XYCoord coord : tiles )
      {
        if( coCast.snowMap[coord.x][coord.y] < Ave.SNOW_THRESHOLD )
        {
          coCast.snowMap[coord.x][coord.y] = Ave.SNOW_THRESHOLD;
          if( gameMap.getEnvironment(coord).weatherType != Weathers.SNOW )
          {
            snowTiles.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(gameMap.getEnvironment(coord).terrainType, Weathers.SNOW), 1));
          }
        }
      }

      // Do all of our terrain alterations.
      GameEventQueue nixEvents = new GameEventQueue();
      nixEvents.add(new MapChangeEvent(snowTiles));

      return nixEvents;
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
    UnitModifier damageMod = null;

    GlacioAbility(Ave commander)
    {
      super(commander, GLACIO_NAME, GLACIO_COST);
      coCast = commander;
      damageMod = new UnitDamageModifier(GLACIO_BUFF);
      AIFlags = PHASE_TURN_END;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      // Normal CO-power boost.
      modList.add(damageMod);
    }

    protected void perform(MapMaster gameMap)
    {
      // Freeze enemies around each of Ave's units or buildings.
      for( Unit victim : findVictims(gameMap) )
        victim.isStunned = true;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Keep track of any tiles that change to snow.
      ArrayList<MapChangeEvent.EnvironmentAssignment> tileChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();

      // Add snow in an expanded range around Ave's areas.
      int maxSnowRange = coCast.MAX_SNOW_SPREAD_RANGE + GLACIO_SNOW_SPREAD;
      Set<XYCoord> tilesInRange = Utils.findLocationsNearPoints(gameMap, coCast.army.getOwnedProperties(), maxSnowRange);
      // This is intended to only count Ave's units, not all units in the Army
      // Perhaps it should count everyone, and Glacio should grant normal snow movement to everyone?
      tilesInRange.addAll(Utils.findLocationsNearUnits(gameMap, coCast.army.getUnits(), GLACIO_SNOW_SPREAD));
      for( XYCoord coord : tilesInRange )
      {
        if( coCast.snowMap[coord.x][coord.y] < Ave.SNOW_THRESHOLD )
        {
          coCast.snowMap[coord.x][coord.y] = Ave.SNOW_THRESHOLD;
          if( gameMap.getEnvironment(coord).weatherType != Weathers.SNOW )
          {
            tileChanges.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(gameMap.getEnvironment(coord).terrainType, Weathers.SNOW), 1));
          }
        }
      }

      GameEventQueue glacioEvents = new GameEventQueue();
      glacioEvents.add(new MapChangeEvent(tileChanges));

      return glacioEvents;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      for( Unit victim : findVictims(gameMap) )
        output.add(new DamagePopup(
                       new XYCoord(victim.x, victim.y),
                       myCommander.myColor,
                       "stun"));

      return output;
    }

    public HashSet<Unit> findVictims(GameMap gameMap)
    {
      HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
      // Should this support all units in my army? It would be a little weird to stun without making snow.
      Set<XYCoord> tilesInRange = Utils.findLocationsNearUnits(gameMap, coCast.army.getUnits(), GLACIO_FREEZE_RANGE);
      tilesInRange.addAll(Utils.findLocationsNearPoints(gameMap, coCast.army.getOwnedProperties(), GLACIO_FREEZE_RANGE));
      for( XYCoord coord : tilesInRange )
      {
        Unit victim = gameMap.getResident(coord);
        if( null != victim && myCommander.isEnemy(victim.CO) )
        {
          victims.add(victim);
        }
      }
      return victims;
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
    private static final int OBLIDO_DAMAGE = 20;

    UnitModifier damageMod = null;
    Ave Ave;

    OblidoAbility(Ave commander)
    {
      super(commander, OBLIDO_NAME, OBLIDO_COST);
      Ave = commander;
      damageMod = new UnitDamageModifier(OBLIDO_BUFF);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
      // One of the big benefits of this power is deforestation, since trees get in Ave's way.
      // Trees are only removed once, so we'll increase cost more slowly to counteract the decreased utility.
      costBasis.starRatioPerCast = 555; // 8 stars * 555 = +4440 per cast, vs the old 9000/2=4500 per cast
      costBasis.maxStarRatio = costBasis.maxScalingCasts * costBasis.starRatioPerCast;
    }

    @Override
    public void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(damageMod);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      // Keep track of any tiles that change.
      ArrayList<MapChangeEvent.EnvironmentAssignment> tileChanges = new ArrayList<MapChangeEvent.EnvironmentAssignment>();

      Set<XYCoord> affectedTiles = getTilesInRange(gameMap);

      // Smash things. Don't add snow though.
      for( XYCoord coord : affectedTiles )
      {
        // Destroy any forests. Big hail, man.
        Environment tileEnvi = gameMap.getEnvironment(coord);
        if(tileEnvi.terrainType == TerrainType.FOREST)
        {
          tileChanges.add(new MapChangeEvent.EnvironmentAssignment(coord, Environment.getTile(TerrainType.GRASS, tileEnvi.weatherType), 1));
        }
      }

      GameEvent damage = new MassDamageEvent(myCommander, findVictims(gameMap, affectedTiles), OBLIDO_DAMAGE, false);

      GameEvent tileChange = new MapChangeEvent(tileChanges);

      // Do all of our terrain alterations.
      GameEventQueue oblidoEvents = new GameEventQueue();
      oblidoEvents.add(damage);
      oblidoEvents.add(tileChange);

      return oblidoEvents;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();
      Set<XYCoord> affectedTiles = getTilesInRange(gameMap);

      for( Unit victim : findVictims(gameMap, affectedTiles) )
      {
        XYCoord coord = new XYCoord(victim.x, victim.y);
        // Forest wrecking takes priority over damage, since it's a permanent map change
        if( gameMap.getEnvironment(coord).terrainType != TerrainType.FOREST )
          output.add(new DamagePopup(
                         coord,
                         myCommander.myColor,
                         Math.min(victim.getHealth()-1, OBLIDO_DAMAGE) + "%"));
      }
      for( XYCoord coord : affectedTiles )
        if( gameMap.getEnvironment(coord).terrainType == TerrainType.FOREST )
          output.add(new DamagePopup(
                         coord,
                         myCommander.myColor,
                         "RAZE"));

      return output;
    }

    public Set<XYCoord> getTilesInRange(GameMap gameMap)
    {
      Set<XYCoord> tilesInRange = Utils.findLocationsNearUnits(gameMap, Ave.army.getUnits(), OBLIDO_RANGE);
      tilesInRange.addAll(Utils.findLocationsNearPoints(gameMap, Ave.army.getOwnedProperties(), OBLIDO_RANGE));
      return tilesInRange;
    }

    public HashSet<Unit> findVictims(GameMap gameMap, Set<XYCoord> tilesInRange)
    {
      HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
      for( XYCoord coord : tilesInRange )
      {
        Unit victim = gameMap.getResident(coord);
        if( null != victim && myCommander.isEnemy(victim.CO) )
        {
          victims.add(victim);
        }
      }
      return victims;
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

  private static class CitySnowifier implements GameEventListener
  {
    private static final long serialVersionUID = 1L;
    Ave Ave;
    public CitySnowifier(Ave cmdr)
    {
      Ave = cmdr;
    }
    @Override
    public GameEventQueue receiveCaptureEvent(Unit unit, Commander prevOwner, MapLocation location)
    {
      GameEventQueue returnEvents = new GameEventQueue();

      if( unit.CO == Ave && (location.getOwner() == Ave) )
      {
        XYCoord where = location.getCoordinates();
        Ave.snowMap[where.x][where.y] += SNOW_THRESHOLD;
        returnEvents.add(
            new MapChangeEvent( where, Environment.getTile(location.getEnvironment().terrainType, Weathers.SNOW) )
            );
      }
      return returnEvents;
    }
  }
}
