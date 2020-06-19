package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import AI.AIUtils;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.TeleportEvent;
import Terrain.Location;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

public class Tech extends Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Tech");
      infoPages.add(new InfoPage(
          "Tech is a first-rate grease monkey who like nothing better than to build new machines and set them loose.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "- Tech has some of the best mechanics around, allowing her units to repair 3 HP per turn instead of just 2.\n"));
      infoPages.add(new InfoPage(
          TECHDROP_NAME + " (" + TECHDROP_COST + "):\n" +
              "Deploys " + TECHDROP_NUM + " BattleMech to the front lines.\n" +
              "All mechanical units gain " + TECHDROP_BUFF + "% attack.\n" +
              "All units gain " + TECHDROP_BUFF + "% Defense.\n"));
      infoPages.add(new InfoPage(
          OVERCHARGE_NAME + " (" + OVERCHARGE_COST + "):\n" +
              "All mechanical units are repaired by " + OVERCHARGE_HEAL + " HP, allowing more than 10 HP for this turn.\n" +
              "All mechanical units gain " + OVERCHARGE_BUFF + "% attack.\n" +
              "All Units gain " + OVERCHARGE_BUFF + "% defense"));
      infoPages.add(new InfoPage(
          STEEL_HAIL_NAME + " (" + STEEL_HAIL_COST + "):\n" +
              "Deploys " + STEEL_HAIL_NUM + " BattleMechs to the front lines.\n" +
              "All mechanical units gain " + STEEL_HAIL_BUFF + "% attack.\n" +
              "All units gain " + STEEL_HAIL_BUFF + "% defense.\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Tech(rules);
    }
  }

  // Variables to characterize this Commander's abilities.
  private static final int TECH_REPAIR = 3; // Used for both D2D heal and ability heal.

  private static final String TECHDROP_NAME = "Tech Drop";
  private static final int TECHDROP_COST = 5;
  private static final int TECHDROP_BUFF = 10;
  private static final int TECHDROP_NUM = 1;
  private static final int TECHDROP_RANGE = 2;

  private static final String OVERCHARGE_NAME = "Overcharge";
  private static final int OVERCHARGE_COST = 6;
  private static final int OVERCHARGE_BUFF = 20;
  private static final int OVERCHARGE_HEAL = 3;

  private static final String STEEL_HAIL_NAME = "Steel Hail";
  private static final int STEEL_HAIL_COST = 9;
  private static final int STEEL_HAIL_BUFF = 20;
  private static final int STEEL_HAIL_NUM = 3;
  private static final int STEEL_HAIL_RANGE = 3;

  // These units get the most benefit from Tech's abilities.
  private static long mechanicalUnits = UnitModel.TANK | UnitModel.HOVER | UnitModel.JET | UnitModel.SEA;


  public Tech(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new TechdropAbility(this, TECHDROP_NAME, TECHDROP_COST, TECHDROP_BUFF, TECHDROP_NUM, TECHDROP_RANGE));
    addCommanderAbility(new OverchargeAbility(this, OVERCHARGE_NAME, OVERCHARGE_COST, OVERCHARGE_BUFF, OVERCHARGE_HEAL));
    addCommanderAbility(new TechdropAbility(this, STEEL_HAIL_NAME, STEEL_HAIL_COST, STEEL_HAIL_BUFF, STEEL_HAIL_NUM, STEEL_HAIL_RANGE));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public int getRepairPower()
  {
    return TECH_REPAIR;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    // End Overcharge. Any units who still have > maxHP get reset to max.
    if( getActiveAbilityName().contentEquals(OVERCHARGE_NAME))
    for(Unit u: units)
    {
      if( u.getPreciseHP() > u.model.maxHP ) u.alterHP(10);
    }

    // Do the normal turn-init stuff.
    GameEventQueue events = super.initTurn(map);

    return events;
  }

  /** Heal all units by the specified amount, allowing HP>10, and provide a buff. */
  private static class OverchargeAbility extends CommanderAbility
  {
    private CODamageModifier damageBuff = null;
    private CODefenseModifier defenseBuff = null;
    private int healAmount;

    public OverchargeAbility(Commander commander, String abilityName, double abilityCost, int buff, int healAmt)
    {
      super(commander, abilityName, abilityCost);

      // Create COModifiers that we can apply when needed.
      damageBuff = new CODamageModifier(buff);
      defenseBuff = new CODefenseModifier(buff);
      healAmount = healAmt;

      // Only mechanical units get the firepower boost.
      long exclude = UnitModel.TROOP;
      ArrayList<UnitModel> models = commander.getAllModels(Tech.mechanicalUnits, true, exclude);
      for( UnitModel m : models )
        damageBuff.addApplicableUnitModel(m);

      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Bump up our power level.
      myCommander.addCOModifier(damageBuff);
      myCommander.addCOModifier(defenseBuff);

      // Overcharge
      for(Unit u: myCommander.units)
      {
        // Only mechanical units get overcharged.
        if( u.model.isNone(UnitModel.TROOP) )
        u.alterHP(healAmount, true);
      }
    }
  }

  /** Drop a MechWarrior onto the most contested point on the battlefront. */
  private static class TechdropAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;

    private CODamageModifier damageBuff = null;
    private CODefenseModifier defenseBuff = null;
    private int dropRange;
    private int numDrops;

    GameEventQueue abilityEvents;

    TechdropAbility(Commander commander, String abilityName, double abilityCost, int buff, int num, int abilityRange)
    {
      super(commander, abilityName, abilityCost);

      // Create COModifiers that we can apply when needed.
      damageBuff = new CODamageModifier(buff);
      defenseBuff = new CODefenseModifier(buff);
      numDrops = num;
      dropRange = abilityRange;
      abilityEvents = new GameEventQueue();

      // Only mechanical units get the firepower boost.
      long exclude = UnitModel.TROOP;
      ArrayList<UnitModel> models = commander.getAllModels(Tech.mechanicalUnits, true, exclude);
      for( UnitModel m : models )
        damageBuff.addApplicableUnitModel(m);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Bump up our power level.
      myCommander.addCOModifier(damageBuff);
      myCommander.addCOModifier(defenseBuff);

      // Perform any events we generated in getEvents().
      while(!abilityEvents.isEmpty())
      {
        abilityEvents.poll().performEvent(gameMap);
      }
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      boolean log = true;
      if( log ) System.out.println("[TechDrop] getEvents() entry");

      // Prep events for `numDrops` deployments. Recalculate landing position between each.
      Set<XYCoord> dropLocs = new HashSet<XYCoord>();
      for( int i = 0; i < numDrops; ++i )
      {
        TeleportEvent techDrop = generateDropEvent(gameMap, log);
        if( null != techDrop )
        {
          // Store the new event.
          abilityEvents.add(techDrop);

          // Place it on the map temporarily so we account for it in subsequent drops.
          dropLocs.add(techDrop.getEndPoint());
          Unit u = techDrop.getUnit();
          XYCoord loc = techDrop.getEndPoint();
          gameMap.moveUnit(u, loc.xCoord, loc.yCoord);
        }
        else
        {
          System.out.println("[TechdropAbility.getEvents] WARNING! Could not find drop location.");
        }
      }

      // Make sure we remove the new units until they actually drop in.
      for( XYCoord dl : dropLocs )
      {
        gameMap.removeUnit(gameMap.getLocation(dl).getResident());
      }

      return abilityEvents;
    }

    private TeleportEvent generateDropEvent(MapMaster gameMap, boolean log)
    {
      // Create a new Unit to drop onto the battlefield.
      long unitFlags = UnitModel.ASSAULT | UnitModel.LAND | UnitModel.TANK; // TODO There has got to be a better way to choose a model.
      Unit techMech = new Unit(myCommander, myCommander.getUnitModel( unitFlags, false ) );
      techMech.isTurnOver = false; // Hit the ground ready to rumble.
      if( myCommander.HQLocation.xCoord >= gameMap.mapWidth / 2 )
        techMech.x = gameMap.mapWidth; // Make the unit face left as it falls to match the rest of the units.

      // We want to find the spot where more muscle will do the most good; i.e. somewhere that is highly contested.
      // Spaces that cannot be traversed, and spaces owned by an enemy Commander are invalid drop locations.
      // Scoring method: Find all friendly and enemy units.
      // Spaces near enemy units are given a score equal to the enemy unit's cost. If multiple enemies are nearby, the higher score is used.
      // Spaces near friendly units are scored the same way, except the highest value is subtracted from the space's score.
      // Spaces containing enemy foot soldiers take the full value of the soldier plus the value of the highest-scoring enemy in attack range.

      // Start by computing friendly values. Note that only the spaces in this collection are eligible landing spaces.
      Map<XYCoord, Integer> friendScores = new HashMap<XYCoord, Integer>();
      for( XYCoord propCoord : myCommander.ownedProperties )
      {
        // Record any locations near owned properties; we want to be able to rescue unprotected structures.
        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, propCoord, 0, dropRange) )
          friendScores.put(xyc, 0);
      }

      Set<XYCoord> invalidDropCoords = new HashSet<XYCoord>();
      for( Unit u : myCommander.units )
      {
        XYCoord uxy = new XYCoord(u.x, u.y);                       // Unit location
        Integer uval = u.model.getCost() * u.getHP();              // Unit value
        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, uxy, dropRange) )
        {
          // No dropping into spaces we can't travel on.
          if( !techMech.model.propulsion.canTraverse(gameMap.getEnvironment(xyc)) )
          {
            invalidDropCoords.add(xyc);
            continue;
          }

          Integer curVal = friendScores.putIfAbsent(xyc, uval);               // Put value if absent
          if( null != curVal ) friendScores.put(xyc, Math.max(curVal, uval)); // Take the larger value.
        }

        // Remove any spaces that already have friends in them - no smashing friends.
        invalidDropCoords.add(uxy); // No stomping on friends!
      }

      // Next calculate unfriendly values. Note that these are only eligible landing spaces if they are also within
      // range of friendly units, but it's easier to just compute all the values and then ignore invalid places.
      ArrayList<XYCoord> enemyCoords = AIUtils.findEnemyUnits(myCommander, gameMap);
      Map<XYCoord, Integer> enemyScores = new HashMap<XYCoord, Integer>();
      for( XYCoord nmexy : enemyCoords )
      {
        Unit nme = gameMap.getLocation(nmexy).getResident();          // Enemy unit
        Integer nmeval = nme.model.getCost() * nme.getHP();           // Enemy value

        if(nmexy.getDistance(myCommander.HQLocation) <= nme.model.movePower && nme.model.hasActionType(UnitActionFactory.CAPTURE))
        {
          if( log ) System.out.println(String.format("%s is too close to HQ. Increasing threat rating:", nme.toStringWithLocation()));
          nmeval *= 100; // More weight if this unit threatens HQ.
        }

        // Assign base scores.
        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, nmexy, 0, dropRange) )
        {
          // No dropping into enemy-held properties.
          Location xycl = gameMap.getLocation(xyc);
          if( xycl.isCaptureable() && (null != xycl.getOwner()) && xycl.getOwner().isEnemy(myCommander))
          {
            invalidDropCoords.add(xyc);
            continue;
          }

          // No dropping into we can't travel on.
          if( !techMech.model.propulsion.canTraverse(gameMap.getEnvironment(xyc)) )
          {
            invalidDropCoords.add(xyc);
            continue;
          }

          Integer curVal = enemyScores.putIfAbsent(xyc, nmeval);               // Put value if absent
          if( null != curVal ) enemyScores.put(xyc, Math.max(curVal, nmeval)); // Keep the larger value.
        }

        // We can't squash non-troops.
        if( !nme.model.isTroop() )
        {
          invalidDropCoords.add(nmexy);
          continue;
        }
        else if( !invalidDropCoords.contains(nmexy) )
        {
          // Valid drop locations containing enemy troops rate higher based on whom we could strike from there.
          int val = enemyScores.get(nmexy); // Currently worth this much.
          if( friendScores.containsKey(nmexy) ) friendScores.put(nmexy, 0); // Remove nearby-friend score penalty when smashing is an option.

          Set<XYCoord> enemyLocations = AIUtils.findPossibleTargets(gameMap, techMech, nmexy);
          if( log ) System.out.println(String.format("Would have %d possible attacks after squashing %s", enemyLocations.size(), nme.toStringWithLocation()));

          int bestAttackVal = 0;
          for( XYCoord targetxy : enemyLocations )
          {
            Unit t = gameMap.getLocation(targetxy).getResident();
            if( null == t ) continue; // We don't give a bonus for proximity to destructable terrain.

            int tval = t.model.getCost() * t.getHP();
            if( bestAttackVal < tval ) bestAttackVal = tval;
            if( log ) System.out.println(String.format("Adding %d to %s for %s", tval, nmexy, t.toStringWithLocation()));
          }
          val += bestAttackVal; // Increase the score of stomping here by the cost of the most expensive unit we can attack.
          if( log ) System.out.println(String.format("Value for %s: %d", nme.toStringWithLocation(), val));
          enemyScores.put(nmexy, val);
        }
      }

      // We only want to drop in near enemies; remove any destinations with no "enemy score".
      Set<XYCoord> friendCoords = new HashSet<XYCoord>(friendScores.keySet());
      for( XYCoord fc : friendCoords )
        if( !enemyScores.keySet().contains(fc) )
          friendScores.remove(fc);

      // If that leaves us with no drop zones, then just reinforce the HQ.
      if( friendScores.isEmpty() )
      {
        if( log ) System.out.println("No valid drop zones near nemy. Reinforcing HQ.");
        for( XYCoord nearHQ : Utils.findLocationsInRange(gameMap, myCommander.HQLocation, 0, dropRange) )
            friendScores.put(nearHQ, 0);
      }

      // Remove any destinations we can't use.
      for(XYCoord fc : invalidDropCoords)
        friendScores.remove(fc);

      // Calculate and store scores in a pri-queue for easy sorting.
      PriorityQueue<ScoredSpace> scoredSpaces = new PriorityQueue<ScoredSpace>();
      for( XYCoord coord: friendScores.keySet() )
      {
        Integer fscore = friendScores.get(coord);
        Integer escore = enemyScores.get(coord);
        if( null == escore ) escore = 0;
        scoredSpaces.add( new ScoredSpace(coord, escore-fscore) );
        if(log) System.out.println("Score for " + coord + " is " + (escore-fscore));
      }

      // If there are no good spaces... this should never happen.
      if( scoredSpaces.isEmpty() )
      {
        System.out.println("[TechDrop.perform] Cannot find suitable landing zone. Aborting!");
        return null;
      }

      // Find all spaces that are equally contested.
      ScoredSpace s1 = scoredSpaces.poll();
      ArrayList<ScoredSpace> equalSpaces = new ArrayList<ScoredSpace>();
      equalSpaces.add(s1);
      if( log ) System.out.println("Could land at " + s1.space.toString());
      while( !scoredSpaces.isEmpty() )
      {
        ScoredSpace ss = scoredSpaces.poll();
        if( ss.score == s1.score )
        {
          equalSpaces.add(ss);
          if( log ) System.out.println("           or " + ss.space.toString());
        }
        else break;
      }

      // Chose one at random if there are multiple equally-valid options.
      int index = 0;
      if( equalSpaces.size() > 1 )
      {
        Random rand = new Random(myCommander.units.size());
        index = rand.nextInt(equalSpaces.size());
      }
      XYCoord landingZone = equalSpaces.get(index).space;
      if(log) System.out.println("Landing at " + landingZone);

      // Create our new unit and the teleport event to put it into place.
      myCommander.units.add(techMech);
      TeleportEvent techDrop = new TeleportEvent(gameMap, techMech, landingZone, TeleportEvent.AnimationStyle.DROP_IN, TeleportEvent.CollisionOutcome.KILL);
      return techDrop;
    }

    private class ScoredSpace implements Comparable<ScoredSpace>
    {
      XYCoord space;
      int score;

      public ScoredSpace(XYCoord space, int score)
      {
        this.space = space;
        this.score = score;
      }
      
      @Override
      public int compareTo(ScoredSpace other)
      {
        return other.score - score;
      }
    }
  }
}
