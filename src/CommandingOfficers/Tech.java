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
import Engine.GameAction.TeleportAction;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.TeleportEvent;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModelScheme;
import Units.WeaponModel;
import Units.MoveTypes.FootMech;

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
              "All units gain " + TECHDROP_BUFF + "% defense.\n" +
              "\nNOTE: BattleMechs require special parts and cannot normally be repaired.\n"));
      infoPages.add(new InfoPage(
          OVERCHARGE_NAME + " (" + OVERCHARGE_COST + "):\n" +
              "All mechanical units are repaired by " + OVERCHARGE_HEAL + " HP, allowing more than 10 HP for this turn.\n" +
              "All mechanical units gain " + OVERCHARGE_BUFF + "% attack.\n" +
              "All Units gain " + OVERCHARGE_BUFF + "% defense"));
      infoPages.add(new InfoPage(
          STEEL_HAIL_NAME + " (" + STEEL_HAIL_COST + "):\n" +
              "Deploys " + STEEL_HAIL_NUM + " BattleMechs to the front lines.\n" +
              "All mechanical units gain " + STEEL_HAIL_BUFF + "% attack.\n" +
              "All units gain " + STEEL_HAIL_BUFF + "% defense.\n" +
              "\nNOTE: BattleMechs require special parts and cannot normally be repaired.\n"));
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
  private static final int TECHDROP_COST = 7;
  private static final int TECHDROP_BUFF = 10;
  private static final int TECHDROP_NUM = 1;
  private static final int TECHDROP_RANGE = 2;

  private static final String OVERCHARGE_NAME = "Overcharge";
  private static final int OVERCHARGE_COST = 5;
  private static final int OVERCHARGE_BUFF = 10;
  private static final int OVERCHARGE_HEAL = 3;

  private static final String STEEL_HAIL_NAME = "Steel Hail";
  private static final int STEEL_HAIL_COST = 14;
  private static final int STEEL_HAIL_BUFF = 20;
  private static final int STEEL_HAIL_NUM = 3;
  private static final int STEEL_HAIL_RANGE = 3;

  private UnitModel BattleMechModel = createBattleMechModel();

  public Tech(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    unitModels.add(BattleMechModel);
    addCommanderAbility(new TechdropAbility(this, TECHDROP_NAME, TECHDROP_COST, BattleMechModel, TECHDROP_BUFF, TECHDROP_NUM, TECHDROP_RANGE));
    addCommanderAbility(new OverchargeAbility(this, OVERCHARGE_NAME, OVERCHARGE_COST, OVERCHARGE_BUFF, OVERCHARGE_HEAL));
    addCommanderAbility(new TechdropAbility(this, STEEL_HAIL_NAME, STEEL_HAIL_COST, BattleMechModel, STEEL_HAIL_BUFF, STEEL_HAIL_NUM, STEEL_HAIL_RANGE));
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
    private CODamageModifier damageBuff;
    private CODefenseModifier defenseBuff;
    private int healAmount;
    private ArrayList<UnitModel> unitsToOverCharge;

    public OverchargeAbility(Commander commander, String abilityName, double abilityCost, int buff, int healAmt)
    {
      super(commander, abilityName, abilityCost);

      // Create COModifiers that we can apply when needed.
      damageBuff = new CODamageModifier(buff);
      defenseBuff = new CODefenseModifier(buff);
      healAmount = healAmt;

      // Only mechanical/non-troop units get the firepower boost.
      long exclude = UnitModel.TROOP;
      unitsToOverCharge = commander.getAllModelsNot(exclude);

      for( UnitModel m : unitsToOverCharge )
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
        if( unitsToOverCharge.contains(u.model) )
          u.alterHP(healAmount, true);
      }
    }
  }

  /** Drop a BattleMech onto the most contested point on the battlefront. */
  private static class TechdropAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;

    private CODamageModifier damageBuff = null;
    private CODefenseModifier defenseBuff = null;
    private int dropRange;
    private int numDrops;
    private UnitModel unitModelToDrop;

    TechdropAbility(Commander commander, String abilityName, double abilityCost, UnitModel unitToDrop, int buff, int num, int abilityRange)
    {
      super(commander, abilityName, abilityCost);

      unitModelToDrop = unitToDrop;

      // Create COModifiers that we can apply when needed.
      damageBuff = new CODamageModifier(buff);
      defenseBuff = new CODefenseModifier(buff);
      numDrops = num;
      dropRange = abilityRange;

      // Only mechanical units get the firepower boost.
      long exclude = UnitModel.TROOP;
      ArrayList<UnitModel> models = commander.getAllModelsNot(exclude);
      for( UnitModel m : models )
        damageBuff.addApplicableUnitModel(m);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Bump up our power level. The actual drops are handled by getEvents().
      myCommander.addCOModifier(damageBuff);
      myCommander.addCOModifier(defenseBuff);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      boolean log = true;
      if( log ) System.out.println("[TechDrop] getEvents() entry");

      GameEventQueue abilityEvents = new GameEventQueue();

      // Prep events for `numDrops` deployments. Recalculate landing position between each.
      Set<XYCoord> dropLocs = new HashSet<XYCoord>();
      for( int i = 0; i < numDrops; ++i )
      {
        TeleportAction techDrop = generateDropEvent(gameMap, dropLocs, log);
        if( null != techDrop )
        {
          // Store the new event.
          dropLocs.add(techDrop.getMoveLocation());
          abilityEvents.addAll(techDrop.getEvents(gameMap));
        }
        else
        {
          System.out.println("[TechdropAbility.getEvents] WARNING! Could not find drop location.");
        }
      }

      // Figure out if this caused the enemy's defeat. The TeleportActions can't track
      // this because multiple units may be smashed at once.
      Map<Commander, Integer> smashes = new HashMap<Commander, Integer>();
      for( XYCoord xyc : dropLocs )
      {
        Unit r = gameMap.getLocation(xyc).getResident();
        if( null != r )
        {
          Integer oldVal = smashes.putIfAbsent(r.CO, 1);
          if( null != oldVal ) smashes.put(r.CO, oldVal+1);
        }
      }
      for( Commander co : smashes.keySet() )
      {
        if( co.units.size() > 1 && co.units.size() <= smashes.get(co) )
          abilityEvents.add(new CommanderDefeatEvent(co));
      }

      return abilityEvents;
    }

    private TeleportAction generateDropEvent(MapMaster gameMap, Set<XYCoord> priorDrops, boolean log)
    {
      // Create a new Unit to drop onto the battlefield.
      Unit techMech = new Unit(myCommander, unitModelToDrop);
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

      // Calculate areas of influence for friendly units.
      for( Unit u : myCommander.units )
      {
        XYCoord uxy = new XYCoord(u.x, u.y);                       // Unit location
        Integer uval = u.model.getCost() * u.getHP();              // Unit value

        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, uxy, dropRange) )
        {
          Integer curVal = friendScores.putIfAbsent(xyc, uval);               // Put value if absent
          if( null != curVal ) friendScores.put(xyc, Math.max(curVal, uval)); // Take the larger value.
        }
      }

      // Account for friendly influence of prior drops.
      for( XYCoord pdc : priorDrops )
      {
        Integer uval = unitModelToDrop.getCost() * techMech.getHP();

        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, pdc, dropRange) )
        {
          if( friendScores.containsKey(xyc) ) // Only update existing scores; don't add new spaces here.
          {
            Integer curVal = friendScores.get(xyc);
            friendScores.put(xyc, Math.max(curVal, uval)); // Take the larger value.
          }
        }
      }

      Set<XYCoord> invalidDropCoords = findInvalidDropCoords(gameMap, friendScores.keySet(), priorDrops);

      // Next calculate unfriendly values. Note that these are only eligible landing spaces if they are also within
      // range of friendly units, but it's easier to just compute all the values and then ignore invalid places.
      ArrayList<XYCoord> enemyCoords = AIUtils.findEnemyUnits(myCommander, gameMap);
      Map<XYCoord, Integer> enemyScores = new HashMap<XYCoord, Integer>();
      for( XYCoord nmexy : enemyCoords )
      {
        if( priorDrops.contains(nmexy) )
          continue; // Ignore enemies that are about to get pasted anyway.

        Unit nme = gameMap.getLocation(nmexy).getResident();          // Enemy unit
        Integer nmeval = nme.model.getCost() * nme.getHP();           // Enemy value

        if(nmexy.getDistance(myCommander.HQLocation) <= nme.model.movePower && nme.model.hasActionType(UnitActionFactory.CAPTURE))
        {
          if( log ) System.out.println(String.format("%s is too close to HQ. Increasing threat rating:", nme.toStringWithLocation()));
          nmeval *= 100; // More weight if this unit threatens HQ.
        }

        // Assign base scores for spaces around this enemy.
        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, nmexy, 0, dropRange) )
        {
          if(invalidDropCoords.contains(xyc))
            continue;
          double discountFactor = 0.5; // Farther spaces are worth less. This encourages up-in-your-facedness.
          Integer discountedVal = (int)(nmeval * Math.pow(discountFactor, xyc.getDistance(nmexy)));
          Integer curVal = enemyScores.putIfAbsent(xyc, discountedVal);                  // Put value if absent
          if( null != curVal ) enemyScores.put(xyc, Math.max(curVal, discountedVal));    // Keep the larger value.
        }

        if( !invalidDropCoords.contains(nmexy) )
        {
          // Valid drop locations containing enemy troops rate higher based on whom we could strike from there.
          int val = enemyScores.get(nmexy); // Currently worth this much.
          if( friendScores.containsKey(nmexy) ) friendScores.put(nmexy, 0); // Remove nearby-friend score penalty when smashing is an option.

          boolean shootTerrain = false;
          Set<XYCoord> enemyLocations = AIUtils.findPossibleTargets(gameMap, techMech, nmexy, shootTerrain);
          if( log ) System.out.println(String.format("Would have %d possible attacks after squashing %s", enemyLocations.size(), nme.toStringWithLocation()));
          int bestAttackVal = 0;
          for( XYCoord targetxy : enemyLocations )
          {
            if( priorDrops.contains(targetxy) || (targetxy.equals(nmexy)) )
              continue; // Ignore enemies that are about to get pasted anyway.

            Unit t = gameMap.getLocation(targetxy).getResident();
            if( null == t ) continue; // We don't give a bonus for proximity to destructible terrain.

            int tval = t.model.getCost() * t.getHP();
            if( bestAttackVal < tval ) bestAttackVal = tval;
            if( log ) System.out.println(String.format("Could add %d to %s for %s", tval, nmexy, t.toStringWithLocation()));
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

      // If there are no good spaces... this should almost never happen.
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
      TeleportAction techDrop = new TeleportAction(techMech, landingZone, TeleportEvent.AnimationStyle.DROP_IN, TeleportEvent.CollisionOutcome.KILL);
      return techDrop;
    }

    private Set<XYCoord> findInvalidDropCoords(MapMaster gameMap, final Set<XYCoord> options, final Set<XYCoord> priorDrops)
    {
      Set<XYCoord> invalidDropCoords = new HashSet<XYCoord>();
      invalidDropCoords.addAll(priorDrops);
      for( XYCoord pdc : options )
      {
        // No unfortunate accidents
        if( !unitModelToDrop.propulsion.canTraverse(gameMap.getEnvironment(pdc)) )
          invalidDropCoords.add(pdc);

        // No trespassing
        Location xycl = gameMap.getLocation(pdc);
        if( xycl.isCaptureable() && (null != xycl.getOwner()) && xycl.getOwner().isEnemy(myCommander) )
          invalidDropCoords.add(pdc);

        Unit resident = xycl.getResident();
        if( null == resident )
          continue;

        // No stomping on friends!
        if( !myCommander.isEnemy(resident.CO) )
          invalidDropCoords.add(pdc);

        // Only stomp on squishies
        if( !resident.model.isTroop() )
          invalidDropCoords.add(pdc);
      }

      return invalidDropCoords;
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
  } // class TechDropAbility

  /**
   * Cobble together a new unit type from existing parts so that we don't have to modify
   * the existing unit enum or damage tables. There are several downsides to this approach,
   * like needing to use existing weapon/chassis types, and the fact that it'll break if
   * we add a new UnitModelScheme with different units or weapons.
   * @return A UnitModelScheme-compliant UnitModel to drop on enemy heads.
   */
  private UnitModel createBattleMechModel()
  {
    UnitModel mdTank = UnitModelScheme.getModelFromString("Md Tank", unitModels);
    UnitModel antiAir = UnitModelScheme.getModelFromString("Anti-Air", unitModels);
    UnitModel BattleMech = mdTank.clone();
    BattleMech.name = "BattleMech";
    BattleMech.role = BattleMech.role | UnitModel.SURFACE_TO_AIR;
    BattleMech.moneyCostAdjustment = mdTank.getCost() + (antiAir.getCost()/2);
    BattleMech.abilityPowerValue = 2.0;
    BattleMech.maxFuel = 30;
    BattleMech.maxAmmo = 10;
    BattleMech.visionRange = 2;
    BattleMech.movePower = 4;
    BattleMech.propulsion = new FootMech();
    BattleMech.healableHabs = new HashSet<TerrainType>(); // BattleMechs have specialized parts, not easy to repair.

    WeaponModel ratatat = antiAir.weapons.get(0).clone();
    ArrayList<WeaponModel> weapons = new ArrayList<WeaponModel>();
    weapons.add(BattleMech.weapons.get(0));
    weapons.add(ratatat);
    BattleMech.weapons = weapons;
    return BattleMech;
  }
} // class Tech
