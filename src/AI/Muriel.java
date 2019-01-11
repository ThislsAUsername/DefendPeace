package AI;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameAction.ActionType;
import Engine.GameActionSet;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleInstance;
import Terrain.Environment;
import Terrain.Environment.Weathers;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;

/**
 * Muriel will Make Units Reactively, Informed by the Enemy Loadout.
 */
public class Muriel implements AIController
{
  private Queue<GameAction> queuedActions = new ArrayDeque<GameAction>();

  private Commander myCo = null;

  private StringBuffer logger = new StringBuffer();
  private int turnNum = 0;

  private ArrayList<Commander> enemyCos = null;
  
  private UnitEffectivenessMap myUnitEffectMap;
  private final double COST_EFFECTIVENESS_THRESHOLD = 0.75;

  private ArrayList<XYCoord> nonAlliedProperties; // set from AIUtils.

  public Muriel(Commander co, Commander[] allCos )
  {
    myCo = co;

    // Initialize UnitModel collections.
    ArrayList<UnitModel> myUnitModels = new ArrayList<UnitModel>();
    enemyCos = new ArrayList<Commander>();
    Map<Commander, ArrayList<UnitModel> > otherUnitModels = new HashMap<Commander, ArrayList<UnitModel> >();
    for( Commander other : allCos )
    {
      if( myCo.isEnemy(other) )
      {
        enemyCos.add(other);
        otherUnitModels.put(other, new ArrayList<UnitModel>());
      }
    }

    // Figure out what I and everyone else can build.
    myUnitModels = myCo.unitModels;
    for( Commander oCo : enemyCos )
    {
      otherUnitModels.put(oCo, oCo.unitModels);
    }

    // Figure out unit matchups.
    myUnitEffectMap = new UnitEffectivenessMap();
    for( UnitModel myModel : myUnitModels )
    {
      for( Commander oCo : enemyCos )
      {
        for( UnitModel otherModel : otherUnitModels.get(oCo) )
        {
          Unit myUnit = new Unit(myCo, myModel);
          Unit otherUnit = new Unit( oCo, otherModel );

          double myDamage = 0;
          Weapon myWeapon = myUnit.chooseWeapon(otherUnit.model, 1, false);
          if( null != myWeapon )
          {
            BattleInstance.BattleParams params = new BattleInstance.BattleParams(myUnit, myWeapon,
                otherUnit, Environment.getTile(TerrainType.ROAD, Weathers.CLEAR));
            myDamage = params.calculateDamage();
          }

          // Now go the other way.
          double otherDamage = 0;
          Weapon otherWeapon = otherUnit.chooseWeapon(myUnit.model, 1, false);
          if( null != otherWeapon )
          {
            BattleInstance.BattleParams params = new BattleInstance.BattleParams(otherUnit, otherWeapon,
                myUnit, Environment.getTile(TerrainType.ROAD, Weathers.CLEAR));
            otherDamage = params.calculateDamage();
          }

          // Calculate and store the damage and cost-effectiveness ratios.
          double ratio = 0;
          double invRatio = 0;
          if( myDamage != 0 && otherDamage != 0)
          {
            ratio = myDamage / otherDamage;
            invRatio = 1/ratio;
          }
          if( myDamage == 0 ) ratio = 0;
          if( otherDamage == 0 ) invRatio = 0;
          if( myDamage != 0 && otherDamage == 0 ) ratio = Double.MAX_VALUE;
          if( myDamage == 0 && otherDamage != 0 ) invRatio = Double.MAX_VALUE;
          double myCostRatio = ratio * ((double)otherModel.getCost() / myModel.getCost());
          double otherCostRatio = invRatio * ((double)myModel.getCost() / otherModel.getCost());
          myUnitEffectMap.put(new UnitModelPair(myModel, otherModel), new UnitMatchupAndMetaInfo(ratio, myCostRatio));
          myUnitEffectMap.put(new UnitModelPair(otherModel, myModel), new UnitMatchupAndMetaInfo(invRatio, otherCostRatio));

          System.out.println(String.format("%s vs %s: %s/%s costRatio: %s", myUnit, otherUnit, myDamage, otherDamage, myCostRatio));
        }
      }
    }
  }

  @Override
  public void initTurn(GameMap gameMap)
  {
    turnNum++;
    log(String.format("[======== Muriel initializing turn %s for %s =========]", turnNum, myCo));

    // Make a list of properties we want to claim.
    nonAlliedProperties = AIUtils.findNonAlliedProperties(myCo, gameMap);

    // If we are already capturing any of these properties, remove them from the list.
    for( Unit unit : myCo.units )
    {
      if( unit.getCaptureProgress() > 0 )
      {
        nonAlliedProperties.remove(unit.getCaptureTargetCoords());
      }
    }

    // If the CO has enough AP, preload the CommanderAbilityAction.
    ArrayList<CommanderAbility> abilities = myCo.getReadyAbilities();
    if( abilities.size() > 0 )
    {
      log("Activating " + abilities.get(0));
      queuedActions.offer(new GameAction.AbilityAction(abilities.get(0)));
    }
  }

  @Override
  public void endTurn()
  {
    log(String.format("[======== Muriel ending turn %s for %s =========]", turnNum, myCo));
    logger = new StringBuffer();
  }

  private void log(String message)
  {
    System.out.println(message);
    logger.append(message).append('\n');
  }

  @Override
  public GameAction getNextAction(GameMap gameMap)
  {
    // If we have actions ready to go, don't bother calculating anything.
    if( !queuedActions.isEmpty() )
    {
      GameAction action = queuedActions.poll();
      log(String.format("  Action: %s", action));
      return action;
    }

    // Handle Unit Actions
    for( Unit unit : myCo.units )
    {
      if( unit.isTurnOver ) continue; // Ignore stale units.

      // If we are capturing something, finish what we started.
      if( unit.getCaptureProgress() > 0 )
      {
        log(String.format("%s is currently capturing; continue", unit.toStringWithLocation()));
        queuedActions.offer(new GameAction.CaptureAction(gameMap, unit, Utils.findShortestPath(unit, unit.x, unit.y, gameMap)));
        break;
      }

      //////////////////////////////////////////////////////////////////
      // If we are currently healing, stick around, unless that would stem the tide of reinforcements.
      Location loc = gameMap.getLocation(unit.x, unit.y);
      if( (unit.getHP() <= 8) && unit.model.canRepairOn(loc) && (loc.getEnvironment().terrainType != TerrainType.FACTORY) && (loc.getOwner() == unit.CO) )
      {
        log(String.format("%s is damaged and on a repair tile. Will continue to repair for now.", unit.toStringWithLocation()));
        ArrayList<GameActionSet> actionSet = unit.getPossibleActions(gameMap, Utils.findShortestPath(unit, unit.x, unit.y, gameMap));
        for( GameActionSet set : actionSet )
        {
          // Go ahead and attack someone as long as we don't have to move.
          if( set.getSelected().getType() == ActionType.ATTACK )
          {
            for( GameAction action : set.getGameActions() )
            {
              Unit other = gameMap.getLocation(action.getTargetLocation()).getResident();
              if( shouldAttack(unit, other, gameMap) )
              {
                log(String.format("  May as well try to shoot %s since I'm here anyway", other));
                queuedActions.offer(action);
                break;
              }
            }
          }
          if( !queuedActions.isEmpty() ) break; // One action per invocation.
        }
        if( queuedActions.isEmpty() )
        {
          // We didn't find someone adjacent to smash, so just sit tight for now.
          queuedActions.offer(new GameAction.WaitAction(unit, Utils.findShortestPath(unit, unit.x, unit.y, gameMap)));
        }
        break;
      } // ~Continue repairing if in a depot.

      //////////////////////////////////////////////////////////////////
      // Figure out if we should go resupply.
      boolean shouldResupply = false;
      // If we are low on fuel.
      if( unit.fuel < (unit.model.maxFuel/4.0) )
      {
        log(String.format("%s is low on fuel.", unit.toStringWithLocation()));
        shouldResupply = true;
      }
      // If we are low on HP, go heal.
      if( unit.getHP() < 6 ) // Arbitrary threshold
      {
        log(String.format("%s is damaged (%s HP).", unit.toStringWithLocation(), unit.getHP()));
        shouldResupply = true;
      }
      // If we are out of ammo.
      if( unit.weapons != null && unit.weapons.size() > 0 )
      {
        for( Weapon weap : unit.weapons )
        {
          if(weap.ammo == 0)
          {
            log(String.format("%s is out of ammo.", unit.toStringWithLocation()));
            shouldResupply = true;
          }
        }
      }
      if( shouldResupply )
      {
        ArrayList<XYCoord> stations = AIUtils.findRepairDepots(unit);
        XYCoord unitCoords = new XYCoord(unit.x, unit.y);
        Utils.sortLocationsByDistance(unitCoords, stations);
        for( XYCoord coord : stations )
        {
          Location station = gameMap.getLocation(coord);
          // Go to the nearest unoccupied friendly space, but don't gum up the production lines.
          if( station.getResident() == null && (station.getEnvironment().terrainType != TerrainType.FACTORY) )
          {
            // Plot a course towards a repair station, but only apply the action if it moves us.
            // If a unit is stuck on the front lines and can't get away past reinforcements, just gotta knuckle up.
            GameAction goHome = AIUtils.moveTowardLocation(unit, coord, gameMap);
            if( !goHome.getMoveLocation().equals(unitCoords) )
            {
              log(String.format("  Heading towards %s to resupply", coord));
              queuedActions.offer(goHome);
              break;
            }
            else
            {
              log(String.format("  Can't find a way to move towards resupply station at %s", coord));
            }
          }
        }
        if( !queuedActions.isEmpty() ) break; // Break so we don't inadvertently plan two actions for this unit.
      }

      // Find all the things we can do from here.
      Map<ActionType, ArrayList<GameAction> > unitActionsByType = AIUtils.getAvailableUnitActionsByType(unit, gameMap);

      //////////////////////////////////////////////////////////////////
      // Look for advantageous attack actions.
      ArrayList<GameAction> attackActions = unitActionsByType.get(ActionType.ATTACK);
      GameAction maxCarnageAction = null;
      double maxDamageValue = 0;
      for( GameAction action : attackActions )
      {
        // Sift through all attack actions we can perform.
        XYCoord targetLoc = action.getTargetLocation();
        Unit target = gameMap.getLocation(targetLoc).getResident();
        Environment environment = gameMap.getEnvironment(targetLoc);

        // Calculate the cost of the damage we can do.
        BattleInstance.BattleParams params = new BattleInstance.BattleParams(unit, unit.chooseWeapon(target.model, 1, true), target, environment);
        double hpDamage = Math.min(params.calculateDamage(), target.getPreciseHP());
        double damageValue = (target.model.getCost()/10) * hpDamage;

        // Find the attack that causes the most monetary damage, provided it's at least a halfway decent idea.
        if( (damageValue > maxDamageValue) && shouldAttack(unit, target, gameMap) )
        {
          maxDamageValue = damageValue;
          maxCarnageAction = action;
        }
      }
      if( maxCarnageAction != null)
      {
        queuedActions.offer(maxCarnageAction);
        break; // Find one action per invocation to avoid overlap.
      }

      //////////////////////////////////////////////////////////////////
      // See if there's something to capture (but only if we are moderately healthy).
      ArrayList<GameAction> captureActions = unitActionsByType.get(ActionType.CAPTURE);
      if( !captureActions.isEmpty() && unit.getHP() >= 7 )
      {
        GameAction capture = captureActions.get(0);
        queuedActions.offer(capture);
        nonAlliedProperties.remove(capture.getTargetLocation());
        break; // One action per call to this function.
      }

      //////////////////////////////////////////////////////////////////
      // We didn't find an immediate ATTACK or CAPTURE action we can do.
      // Things that can capture; go find something to capture, if you are moderately healthy.
      if( unit.model.hasActionType(ActionType.CAPTURE) && (unit.getHP() >= 7) )
      {
        log(String.format("Seeking capture target for %s", unit.toStringWithLocation()));
        XYCoord unitCoords = new XYCoord(unit.x, unit.y);
        Utils.sortLocationsByDistance(unitCoords, nonAlliedProperties);
        for(int i = 0; i < nonAlliedProperties.size(); ++i)
        {
          XYCoord coord = nonAlliedProperties.get(i);
          GameAction move = AIUtils.moveTowardLocation(unit, coord, gameMap); // Try to move there, but try not to sit on a factory, and don't just sit still.
          if( null != move && (gameMap.getLocation(move.getMoveLocation()).getEnvironment().terrainType != TerrainType.FACTORY) && !unitCoords.equals(move.getMoveLocation()))
          {
            log(String.format("  Found %s at %s", gameMap.getLocation(coord).getEnvironment().terrainType, coord));
            queuedActions.offer(move);
            break;
          }
        }
        if( !queuedActions.isEmpty() ) break; // One action per invocation.
      }

      //////////////////////////////////////////////////////////////////
      // Everyone else, go hunting.
      if( queuedActions.isEmpty() && unit.model.hasActionType(ActionType.ATTACK) )
      {
        log(String.format("Seeking attack target for %s", unit.toStringWithLocation()));
        ArrayList<XYCoord> enemyLocations = AIUtils.findEnemyUnits(myCo, gameMap); // Get enemy locations.
        Utils.sortLocationsByDistance(new XYCoord(unit.x, unit.y), enemyLocations); // Sort them by accessibility.
        GameAction move = null;
        for(int i = 0; i < enemyLocations.size(); ++i)
        {
          XYCoord coord = enemyLocations.get(i);
          Unit target = gameMap.getLocation(coord).getResident();

          // Only chase this unit if we will be effective against it. Don't check shouldAttack here, because we can't actually attack yet.
          if( myUnitEffectMap.get(new UnitModelPair(unit.model, target.model)).costEffectivenessRatio < COST_EFFECTIVENESS_THRESHOLD ) continue;

          // Try to move towards the enemy, but avoid blocking a factory.
          move = AIUtils.moveTowardLocation(unit, coord, gameMap);
          if( null != move && (gameMap.getLocation(move.getMoveLocation()).getEnvironment().terrainType != TerrainType.FACTORY))
          {
            log(String.format("  Found %s", gameMap.getLocation(coord).getResident().toStringWithLocation()));
            queuedActions.offer(move);
            break;
          }
        }
        if( !queuedActions.isEmpty() ) break; // One action per invocation.
      }

      if( queuedActions.isEmpty() )
      {
        // Couldn't find any capture or attack actions. This unit is
        // either a transport, or stranded on an island somewhere.
        log(String.format("Could not find an action for %s. Waiting", unit.toStringWithLocation()));
        queuedActions.offer(new GameAction.WaitAction(unit, Utils.findShortestPath(unit, unit.x, unit.y, gameMap)));
      }
    } // ~Unit action loop

    // If we don't have anything else to do, build units.
    if( queuedActions.isEmpty() )
    {
      queueUnitProductionActions(gameMap);
    }

    GameAction action = queuedActions.poll();
    log(String.format("  Action: %s", action));
    return action;
  }

  private boolean shouldAttack(Unit unit, Unit target, GameMap gameMap)
  {
    // Calculate the cost of the damage we can do.
    BattleInstance.BattleParams params = new BattleInstance.BattleParams(unit, unit.chooseWeapon(target.model, 1, true), target, gameMap.getLocation(target.x, target.y).getEnvironment());
    double damage = params.calculateDamage();
    UnitMatchupAndMetaInfo umami = myUnitEffectMap.get(new UnitModelPair(unit.model, target.model));

    // This attack is a good idea if our cost effectiveness is in the acceptable range, or if we can at least half-kill them.
    // The second check is needed because one glass cannon may not have a great overall ratio against another; whoever hits first wins, e.g. Mech vs Anti-Air.
    return (umami.costEffectivenessRatio > COST_EFFECTIVENESS_THRESHOLD) || (damage > (target.getHP() / 2.0));
  }

  private void queueUnitProductionActions(GameMap gameMap)
  {
    log("Evaluating Production needs");
    int budget = myCo.money;

    // Get a count of enemy forces.
    Map<UnitModel, Double> enemyUnitCounts = new HashMap<UnitModel, Double>();
    for( Commander co : enemyCos )
    {
      for( Unit u : co.units )
      {
        if( !u.model.hasDirectFireWeapon() ) continue; // Only handle direct-fire units for now.
        // Count how many of each model of enemy units are in play.
        if( enemyUnitCounts.containsKey(u.model))
        {
          enemyUnitCounts.put(u.model, enemyUnitCounts.get(u.model) + (u.getHP() / 10) );
        }
        else
        {
          enemyUnitCounts.put(u.model, u.getHP() / 10.0 );
        }
      }
    }

    // Figure out what unit types we can purchase with our available properties.
    CommanderProductionInfo CPI = new CommanderProductionInfo(myCo, gameMap);

    if( CPI.availableProperties.isEmpty() )
    {
      log("No properties available to build.");
      return;
    }

    // Sort enemy units by cardinality. We will attempt to build counters for the least numerous first.
    // The most numerous enemies are probably cheap, and also countered by whatever we build for the narrow case.
    ArrayList<UnitModel> enemyModels = new ArrayList<UnitModel>();
    ArrayList<Entry<UnitModel, Double>> entryArray = new ArrayList<Entry<UnitModel, Double>>(enemyUnitCounts.entrySet());
    Collections.sort(entryArray, new UnitQuantityComparator());
    for( Entry<UnitModel, Double> ent : entryArray )
    {
      enemyModels.add(ent.getKey());
    }

    // Try to purchase units that will counter the most-represented enemies.
    ArrayList<PurchaseOrder> shoppingCart = new ArrayList<PurchaseOrder>();
    while( !enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())
    {
      // Find the first (least numerous) enemy UnitModel, and remove it. Even if we can't find an adequate counter,
      // there is not reason to consider it again on the next iteration.
      UnitModel enemyToCounter = enemyModels.get(0);
      enemyModels.remove(enemyToCounter);
      double enemyNumber = enemyUnitCounts.get(enemyToCounter);
      log(String.format("Need a counter for %sx%s", enemyToCounter, enemyNumber));
      log(String.format("Remaining budget: %s", budget));

      // Get our possible options for countermeasures.
      ArrayList<UnitModel> availableUnitModels = new ArrayList<UnitModel>(CPI.availableUnitModels);
      while( !availableUnitModels.isEmpty() )
      {
        // Sort my available models by their cost-effectiveness against this enemy type.
        Collections.sort(availableUnitModels, new UnitMatchupComparator(enemyToCounter, myUnitEffectMap, UnitMatchupComparator.ComparisonType.COST_RATIO));
        Collections.reverse(availableUnitModels); // Best/highest cost ratio first.

        // Grab the best counter.
        UnitModel idealCounter = availableUnitModels.get(0);
        availableUnitModels.remove(idealCounter); // Make sure we don't try to build two rounds of the same thing in one turn.
        UnitMatchupAndMetaInfo umami = myUnitEffectMap.get(new UnitModelPair(idealCounter, enemyToCounter));
        log(String.format("  %s has cost ratio %s", idealCounter, umami.costEffectivenessRatio));
        if( umami.costEffectivenessRatio < COST_EFFECTIVENESS_THRESHOLD )
        {
          log("    too low; skipping this option");
          continue;
        }

        // Figure out how many of idealCounter we want, and how many we can actually build.
        int numberToBuy = (int)Math.ceil((enemyNumber / umami.damageRatio)); // This is the number we would buy ideally.
        if( numberToBuy == 0 ) numberToBuy = 1;
        log(String.format("    Would like to build %s of them", numberToBuy));
        int maxBuildable = CPI.getNumFacilitiesFor(idealCounter);
        log(String.format("    Facilities available: %s", maxBuildable));
        if( numberToBuy > maxBuildable ) numberToBuy = maxBuildable; // This is the number we have production for right now.
        int totalCost = numberToBuy * idealCounter.getCost();

        // Calculate a cost buffer to ensure we have enough money left so that no factories sit idle.
        UnitModel infModel = myCo.getUnitModel(UnitModel.UnitEnum.INFANTRY);
        int costBuffer = (CPI.getNumFacilitiesFor(infModel)-1) * infModel.getCost(); // The -1 assumes we will build this unit from a factory. Possibly untrue.
        while( totalCost > (budget - costBuffer) ) // This finds how many we can afford.
        {
          totalCost -= idealCounter.getCost();
          numberToBuy--;
        }
        if( numberToBuy > 0 )
        {
          // Go place orders.
          log(String.format("    I can build %s %s, for a cost of %s", numberToBuy, idealCounter, totalCost));
          for( int i = 0; i < numberToBuy; ++i )
          {
            Location loc = CPI.getLocationToBuild(idealCounter);
            shoppingCart.add(new PurchaseOrder(loc, idealCounter));
            budget -= idealCounter.getCost();
            CPI.removeBuildLocation(loc);
          }
          // We found a counter for this enemy UnitModel; break and go to the next type.
          // This break means we will build at most one type of unit per turn to counter each enemy type.
          break;
        }
        else {log(String.format("    %s cost %s, I have %s (witholding %s).", idealCounter, idealCounter.getCost(), budget, costBuffer));}
      } // ~while( !availableUnitModels.isEmpty() )
    } // ~while( !enemyModels.isEmpty() && !CPI.availableUnitModels.isEmpty())

    // Build infantry from any remaining facilities.
    UnitModel infModel = myCo.getUnitModel(UnitModel.UnitEnum.INFANTRY);
    while( (budget >= infModel.getCost()) && (CPI.availableUnitModels.contains(infModel)) )
    {
      Location loc = CPI.getLocationToBuild(infModel);
      shoppingCart.add(new PurchaseOrder(loc, infModel));
      CPI.removeBuildLocation(loc);
    }

    // Convert our PurchaseOrders into GameActions.
    for( PurchaseOrder order : shoppingCart )
    {
      queuedActions.offer(new GameAction.UnitProductionAction(myCo, order.model, order.location.getCoordinates()));
    }
  }

  private static class CommanderProductionInfo
  {
    Commander myCo;
    Set<UnitModel> availableUnitModels;
    Set<Location> availableProperties;
    Map<Terrain.TerrainType, Integer> propertyCounts;
    Map<UnitModel, TerrainType> modelToTerrainMap;

    public CommanderProductionInfo(Commander co, GameMap gameMap)
    {
      // Figure out what unit types we can purchase with our available properties.
      myCo = co;
      availableUnitModels = new HashSet<UnitModel>();
      availableProperties = new HashSet<Location>();
      propertyCounts = new HashMap<Terrain.TerrainType, Integer>();
      modelToTerrainMap = new HashMap<UnitModel, TerrainType>();

      for( XYCoord xyc : co.ownedProperties )
      {
        Location loc = co.myView.getLocation(xyc);
        if( gameMap.isLocationEmpty(loc.getCoordinates()))
        {
          ArrayList<UnitModel> models = co.getShoppingList(loc);
          availableUnitModels.addAll(models);
          availableProperties.add(loc);
          TerrainType terrain = loc.getEnvironment().terrainType;
          if( propertyCounts.containsKey(terrain))
          {
            propertyCounts.put(terrain, propertyCounts.get(loc.getEnvironment().terrainType)+1);
          }
          else
          {
            propertyCounts.put(terrain, 1);
          }

          // Store a mapping from UnitModel to the TerrainType that can produce it.
          // NOTE: This code currently assumes only one TerrainType can produce each type of UnitModel.
          for( UnitModel m : models )
          {
            modelToTerrainMap.put(m, loc.getEnvironment().terrainType );
          }
        }
      }
    }

    public Location getLocationToBuild(UnitModel model)
    {
      TerrainType desiredTerrain = modelToTerrainMap.get(model);
      Location location = null;
      for( Location loc : availableProperties )
      {
        if( loc.getEnvironment().terrainType == desiredTerrain )
        {
          location = loc;
          break;
        }
      }
      return location;
    }

    public void removeBuildLocation(Location loc)
    {
      availableProperties.remove(loc);
      TerrainType terrain = loc.getEnvironment().terrainType;
      if( propertyCounts.containsKey(terrain) )
      {
        propertyCounts.put(terrain, propertyCounts.get(terrain) - 1);
        if( propertyCounts.get(terrain) == 0 )
        {
          availableUnitModels.removeAll(myCo.getShoppingList(loc));
        }
      }
    }

    public int getNumFacilitiesFor(UnitModel model)
    {
      int num = (modelToTerrainMap.containsKey(model)) ? propertyCounts.get(modelToTerrainMap.get(model)) : 0;
      return num;
    }
  }

  private static class PurchaseOrder implements Comparable<PurchaseOrder>
  {
    Location location;
    UnitModel model;

    public PurchaseOrder(Location loc, UnitModel um)
    {
      location = loc;
      model = um;
    }

    @Override
    public int compareTo(PurchaseOrder other)
    {
      return model.getCost() - other.model.getCost();
    }
  }

  /**
   * Sort units by quantity in ascending order.
   */
  private static class UnitQuantityComparator implements Comparator<Entry<UnitModel, Double>>
  {
    @Override
    public int compare(Entry<UnitModel, Double> entry1, Entry<UnitModel, Double> entry2)
    {
      double diff = entry1.getValue() - entry2.getValue();
      return (int)(diff*10); // Multiply by 10 since we return an int, but don't want to lose the decimal-level discrimination.
    }
  }

  /**
   * Arrange UnitModels according to their effectiveness against a configured UnitModel.
   * UnitModels that will be worse against 
   */
  private static class UnitMatchupComparator implements Comparator<UnitModel>
  {
    /**
     *  DAMAGE_RATIO will sort based on how effective each unitType is against targetType.
     *  COST_RATIO will sort based on how cost-effective each unitType is against targetType.
     *  One is more useful if you have fielded units; the other is more useful if you still need to build them.
     */
    public enum ComparisonType { DAMAGE_RATIO, COST_RATIO };

    UnitModel targetModel;
    UnitEffectivenessMap myUem;
    ComparisonType myComparisonType;

    public UnitMatchupComparator(UnitModel targetType, UnitEffectivenessMap uem, ComparisonType compareType)
    {
      targetModel = targetType;
      myUem = uem;
      myComparisonType = compareType;
    }

    @Override
    public int compare(UnitModel model1, UnitModel model2)
    {
      double eff1 = 0;
      double eff2 = 0;
      if( myComparisonType == ComparisonType.DAMAGE_RATIO )
      {
        eff1 = myUem.get(new UnitModelPair(model1, targetModel)).damageRatio;
        eff2 = myUem.get(new UnitModelPair(model2, targetModel)).damageRatio;
      }
      else if( myComparisonType == ComparisonType.COST_RATIO )
      {
        eff1 = myUem.get(new UnitModelPair(model1, targetModel)).costEffectivenessRatio;
        eff2 = myUem.get(new UnitModelPair(model2, targetModel)).costEffectivenessRatio;
      }

      return (eff1 < eff2)? -1 : ((eff1 > eff2)? 1 : 0);
    }
  }

  private static class UnitModelPair
  {
    public final UnitModel first;
    public final UnitModel second;
    public UnitModelPair(UnitModel first, UnitModel second)
    {
      this.first = first;
      this.second = second;
    }

    @Override
    public int hashCode()
    {
      final int prime = 160091;
      int result = 1;
      result = prime * result + first.type.ordinal();
      result = prime * result + second.type.ordinal();
      return result;
    }

    @Override
    public boolean equals(Object obj)
    {
      if( this == obj )
        return true;
      if( obj == null )
        return false;
      if( getClass() != obj.getClass() )
        return false;
      UnitModelPair other = (UnitModelPair) obj;
      if( first != other.first )
        return false;
      if( second != other.second )
        return false;
      return true;
    }
  }

  private static class UnitMatchupAndMetaInfo
  {
    public final double damageRatio;
    public final double costEffectivenessRatio;

    public UnitMatchupAndMetaInfo(double dmgRatio, double costRatio)
    {
      damageRatio = dmgRatio;
      costEffectivenessRatio = costRatio;
    }
  }

  /** Stores an object with info about how well UnitModelPair.first fares against UnitModelPair.second on average. */
  private class UnitEffectivenessMap extends HashMap<UnitModelPair, UnitMatchupAndMetaInfo>
  {private static final long serialVersionUID = -4954625729036690735L;}
}
