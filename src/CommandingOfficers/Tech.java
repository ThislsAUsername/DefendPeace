package CommandingOfficers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import AI.AICombatUtils;
import AI.AIUtils;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.UnitTypeFilter;
import Engine.GameAction.UnitSpawnAction;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitModifier;
import Engine.GameEvents.CreateUnitEvent;
import Terrain.GameMap;
import Terrain.MapLocation;
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
          "Tech is a first-rate grease monkey who likes nothing better than to build new machines and set them loose.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "- Tech has some of the best mechanics around, allowing her units to repair 3 HP per turn instead of just 2.\n"));
      infoPages.add(new InfoPage(
          TECHDROP_NAME + " (" + TECHDROP_COST + "):\n" +
              "Deploys " + TECHDROP_NUM + " BattleMech to the front lines\n" +
              "+" + TECHDROP_BUFF + "% attack for all mechanical units\n" +
              "+" + TECHDROP_BUFF + "% defense for all units\n" +
              "\nNOTE: BattleMechs require special parts and cannot be repaired on buildings\n"));
      infoPages.add(new InfoPage(
          OVERCHARGE_NAME + " (" + OVERCHARGE_COST + "):\n" +
              "+" + OVERCHARGE_HEAL + " HP for all mechanical units, allowing more than 10 HP for this turn\n" +
              "+" + OVERCHARGE_BUFF + "% attack for all mechanical units\n" +
              "+" + OVERCHARGE_BUFF + "% defense for all units"));
      infoPages.add(new InfoPage(
          STEEL_HAIL_NAME + " (" + STEEL_HAIL_COST + "):\n" +
              "Deploys " + STEEL_HAIL_NUM + " BattleMechs to the front lines\n" +
              "+" + STEEL_HAIL_BUFF + "% attack for all mechanical units\n" +
              "+" + STEEL_HAIL_BUFF + "% defense for all units\n" +
              "\nNOTE: BattleMechs require special parts and cannot be repaired on buildings\n"));
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

  // TODO: Support BattleMechs on other COs to enable tags mode?
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

  public static ArrayList<UnitModel> getMechanicalModels(Commander commander)
  {
    long exclude = UnitModel.TROOP;
    ArrayList<UnitModel> typesToOverCharge = commander.getAllModelsNot(exclude);
    return typesToOverCharge;
  }

  /** Heal all units by the specified amount, allowing HP>10, and provide a buff. */
  private static class OverchargeAbility extends CommanderAbility
  {
    private int buff;
    private int healAmount;
    private HashMap<Commander, ArrayList<Unit>> unitsOverCharged = new HashMap<Commander, ArrayList<Unit>>();

    public OverchargeAbility(Tech tech, String abilityName, double abilityCost, int buff, int healAmt)
    {
      super(tech, abilityName, abilityCost);

      this.buff = buff;
      healAmount = healAmt;

      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    private static final long serialVersionUID = 1L;

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      // Only mechanical/non-troop units get the firepower boost.
      UnitTypeFilter damageBuff = new UnitTypeFilter(new CODamageModifier(buff));
      damageBuff.noneOf = UnitModel.TROOP;
      UnitModifier defenseBuff = new CODefenseModifier(buff);
      modList.add(damageBuff);
      modList.add(defenseBuff);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      ArrayList<UnitModel> typesToOverCharge = getMechanicalModels(myCommander);
      ArrayList<Unit> overCharged = new ArrayList<Unit>();
      // Overcharge
      for(Unit u: myCommander.units)
      {
        if( typesToOverCharge.contains(u.model) )
        {
          // Track units that aren't already overhealed
          if( u.getHP() <= u.model.maxHP
              && u.getHP() + healAmount > u.model.maxHP )
            overCharged.add(u);
          u.alterHP(healAmount, true);
        }
      }
      unitsOverCharged.put(myCommander, overCharged);
    }

    @Override
    protected void revert(MapMaster gameMap)
    {
      if( unitsOverCharged.containsKey(myCommander) )
      {
        // End Overcharge. Any units who still have > maxHP get reset to max.
        for( Unit u : unitsOverCharged.get(myCommander) )
        {
          if( u.getPreciseHP() > u.model.maxHP )
            u.alterHP(10);
        }
        unitsOverCharged.remove(myCommander);
      }
    }
  }

  /** Drop a BattleMech onto the most contested point on the battlefront. */
  // TODO: Should this really drop mechs for every CO it affects?
  private static class TechdropAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final boolean log = false;

    private int buff;
    private int dropRange;
    private int numDrops;
    private UnitModel unitModelToDrop;

    TechdropAbility(Tech tech, String abilityName, double abilityCost, UnitModel unitToDrop, int buff, int num, int abilityRange)
    {
      super(tech, abilityName, abilityCost);

      unitModelToDrop = unitToDrop;

      this.buff = buff;
      numDrops = num;
      dropRange = abilityRange;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      // Only mechanical/non-troop units get the firepower boost.
      UnitTypeFilter damageBuff = new UnitTypeFilter(new CODamageModifier(buff));
      damageBuff.noneOf = UnitModel.TROOP;
      UnitModifier defenseBuff = new CODefenseModifier(buff);
      modList.add(damageBuff);
      modList.add(defenseBuff);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      if( log ) System.out.println("[TechDrop] getEvents() entry");

      GameEventQueue abilityEvents = new GameEventQueue();

      // Prep events for `numDrops` deployments. Recalculate landing position between each.
      Set<XYCoord> dropLocs = new HashSet<XYCoord>();
      for( int i = 0; i < numDrops; ++i )
      {
        UnitSpawnAction techDrop = generateDropEvent(myCommander, gameMap, dropLocs, log);
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

      // Figure out if this caused the enemy's defeat. The actions can't track
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

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      Set<XYCoord> dropLocs = new HashSet<XYCoord>();
      for( int i = 0; i < numDrops; ++i )
      {
        XYCoord techDrop = findDropLocation(myCommander, gameMap, dropLocs, log);
        if( null != techDrop )
          dropLocs.add(techDrop);
        else
        {
          System.out.println("[TechdropAbility.getEvents] WARNING! Could not find drop location.");
        }
      }
      for( XYCoord drop : dropLocs )
        output.add(new DamagePopup(
                       drop,
                       myCommander.myColor,
                       "DROP"));

      return output;
    }

    private UnitSpawnAction generateDropEvent(Commander myCommander, MapMaster gameMap, Set<XYCoord> priorDrops, boolean log)
    {
      XYCoord landingZone = findDropLocation(myCommander, gameMap, priorDrops, log);

      // Create our new unit and the teleport event to put it into place.
      boolean stomp = true;
      boolean ready = true;
      UnitSpawnAction techDrop = new UnitSpawnAction(myCommander, unitModelToDrop, landingZone,
          CreateUnitEvent.AnimationStyle.DROP_IN, stomp, ready);
      return techDrop;
    }
    private XYCoord findDropLocation(Commander myCommander, GameMap gameMap, Set<XYCoord> priorDrops, boolean log)
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

      Set<XYCoord> invalidDropCoords = findInvalidDropCoords(myCommander, gameMap, friendScores.keySet(), priorDrops);

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

        if(nmexy.getDistance(myCommander.HQLocation) <= nme.getMovePower(gameMap) && nme.model.hasActionType(UnitActionFactory.CAPTURE))
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
          Set<XYCoord> enemyLocations = AICombatUtils.findPossibleTargets(gameMap, techMech, nmexy, shootTerrain);
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
        System.out.println("[TechDrop.findDropLocation] Cannot find suitable landing zone. Aborting!");
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
        Random rand = new Random(gameMap.mapWidth*gameMap.mapHeight);
        index = rand.nextInt(equalSpaces.size());
      }
      XYCoord landingZone = equalSpaces.get(index).space;
      if(log) System.out.println("Landing at " + landingZone);

      return landingZone;
    }

    private Set<XYCoord> findInvalidDropCoords(Commander myCommander, GameMap gameMap, final Set<XYCoord> options, final Set<XYCoord> priorDrops)
    {
      Set<XYCoord> invalidDropCoords = new HashSet<XYCoord>();
      invalidDropCoords.addAll(priorDrops);
      for( XYCoord pdc : options )
      {
        // No unfortunate accidents
        if( !unitModelToDrop.propulsion.canTraverse(gameMap.getEnvironment(pdc)) )
          invalidDropCoords.add(pdc);

        // No trespassing
        MapLocation xycl = gameMap.getLocation(pdc);
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
    BattleMech.costShift = mdTank.getCost() + (antiAir.getCost()/2);
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
