package CommandingOfficers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import AI.AIUtils;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameScenario;
import Engine.Utils;
import Engine.XYCoord;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.TeleportEvent;
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
          "Tech is a first-rate grease monkey who like nothing better than to create and deploy new technologies'\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "- Tech has some of the best mechanics around, allowing her units to repair 3 HP per turn instead of just 2.\n"));
      infoPages.add(new InfoPage(
          TECHDROP_NAME + " (" + TECHDROP_COST + "):\n" +
          "Gives an attack boost of " + TECHDROP_BUFF + " to all units and deploys a Mechwarrior to the front lines.\n"));
      infoPages.add(new InfoPage(
          OVERCHARGE_NAME + " (" + OVERCHARGE_COST + "):\n" +
          "Gives an attack boost of "+ OVERCHARGE_BUFF +
          " and heals all units by " + OVERCHARGE_HEAL + ", allowing HP>10 for this turn.\n"));
      infoPages.add(new InfoPage(
          SPECIAL_DELIVERY_NAME + " (" + SPECIAL_DELIVERY_COST + "):\n" +
          "Gives an attack boost of " + SPECIAL_DELIVERY_BUFF + " to all units and deploys three Mechwarriors to the front lines.\n"));
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
  private static final int TECHDROP_RANGE = 2;

  private static final String OVERCHARGE_NAME = "Overcharge";
  private static final int OVERCHARGE_COST = 6;
  private static final int OVERCHARGE_BUFF = 20;
  private static final int OVERCHARGE_HEAL = 3;

  private static final String SPECIAL_DELIVERY_NAME = "Special Delivery";
  private static final int SPECIAL_DELIVERY_COST = 9;
  private static final int SPECIAL_DELIVERY_BUFF = 20;

  public Tech(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new TechdropAbility(this, TECHDROP_NAME, TECHDROP_COST, TECHDROP_BUFF, TECHDROP_RANGE));
//    addCommanderAbility(new PatchAbility(this, OVERHEAL_NAME, OVERHEAL_COST, PILLAGE_INCOME, OVERHEAL_BUFF));

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

  /** Drop a MechWarroir onto the most contested point on the battlefront. */
  private static class TechdropAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;

    private CODamageModifier damageBuff = null;
    private CODefenseModifier defenseBuff = null;
    private int dropRange;

    GameEventQueue abilityEvents;

    TechdropAbility(Commander myCO, String abilityName, int abilityCost, int buff, int abilityRange)
    {
      super(myCO, abilityName, abilityCost);

      // Create a COModifier that we can apply to Patch when needed.
      damageBuff = new CODamageModifier(buff);
      defenseBuff = new CODefenseModifier(buff);
      dropRange = abilityRange;
      abilityEvents = new GameEventQueue();
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
      // We want to find the spot where more muscle will do the most good; i.e. somewhere that is highly contested.
      // Score spaces by subtracting the value of nearby friendlies from the value of nearby enemies.
      // The result is how much cost-advantage the enemy has in that space.

      // Start by computing friendly values. Note that only the spaces in this collection are eligible landing spaces.
      Map<XYCoord, Integer> friendScores = new HashMap<XYCoord, Integer>();
      for( XYCoord propCoord : myCommander.ownedProperties )
      {
        // Record any locations near owed properties; we want to be able to rescue unprotected structures.
        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, propCoord, dropRange) )
          friendScores.put(xyc, 0);
      }
      for( Unit u : myCommander.units )
      {
        XYCoord uxy = new XYCoord(u.x, u.y);                       // Unit location
        Integer uval = u.model.getCost() * u.getHP();              // Unit value
        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, uxy, dropRange) )
        {
          Integer curVal = friendScores.putIfAbsent(xyc, uval);      // Put value if absent
          if( null != curVal ) friendScores.put(xyc, curVal + uval); // Combine values if already present
        }

        // Remove any spaces that already have friends in them - no smashing friends.
        friendScores.remove(uxy);
      }

      // Next calculate unfriendly values. Note that these are only eligible landing spaces if they are also within
      // range of friendly units, but it's easier to just compute all the values and then ignore invalid places.
      ArrayList<XYCoord> enemyCoords = AIUtils.findEnemyUnits(myCommander, gameMap);
      Map<XYCoord, Integer> enemyScores = new HashMap<XYCoord, Integer>();
      for( XYCoord nmexy : enemyCoords )
      {
        Unit nme = gameMap.getLocation(nmexy).getResident();          // Enemy unit
        Integer nmeval = nme.model.getCost() * nme.getHP();           // Enemy value

        if(nmexy.getDistance(myCommander.HQLocation) < nme.model.movePower && nme.model.isTroop())
          nmeval *= 12; // More weight if this unit threatens HQ.

        for( XYCoord xyc : Utils.findLocationsInRange(gameMap, nmexy, 0, dropRange) )
        {
          Integer curVal = enemyScores.putIfAbsent(xyc, nmeval);      // Put value if absent
          if( null != curVal ) enemyScores.put(xyc, curVal + nmeval); // Combine values if already present
        }

        if( nme.model.isTroop() )
          enemyScores.put(nmexy, enemyScores.get(nmexy) + nmeval);    // Bonus points for squashing infantry.
        else enemyScores.remove(nmexy);                               // We can't squash larger things.
      }

      // Calculate and store scores in a pri-queue for easy sorting.
      PriorityQueue<ScoredSpace> scoredSpaces = new PriorityQueue<ScoredSpace>();
      for( XYCoord coord: friendScores.keySet() )
      {
        Integer fscore = friendScores.get(coord);
        Integer escore = enemyScores.get(coord);
        if( null == escore ) escore = 0;
        scoredSpaces.add( new ScoredSpace(coord, escore-fscore) );
      }

      // If there are no good spaces... this should never happen.
      if( scoredSpaces.isEmpty() )
      {
        System.out.println("[TechDrop.perform] Cannot find suitable landing zone. Aborting!");
        return abilityEvents;
      }

      // Find all spaces that are equally contested.
      ScoredSpace s1 = scoredSpaces.poll();
      ArrayList<ScoredSpace> equalSpaces = new ArrayList<ScoredSpace>();
      equalSpaces.add(s1);
      while( !scoredSpaces.isEmpty() )
      {
        ScoredSpace ss = scoredSpaces.poll();
        if( ss.score == s1.score )
        {
          equalSpaces.add(ss);
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

      // Create our new unit and the teleport event to put it into place.
      long unitFlags = UnitModel.ASSAULT | UnitModel.LAND | UnitModel.TANK; // TODO There has got to be a better way to choose a model.
      Unit dropIn = new Unit(myCommander, myCommander.getUnitModel( unitFlags, false ) );
      dropIn.isTurnOver = false; // Hit the ground ready to rumble.
      myCommander.units.add(dropIn);
      TeleportEvent techDrop = new TeleportEvent(gameMap, dropIn, landingZone, TeleportEvent.AnimationStyle.DROP_IN, TeleportEvent.CollisionOutcome.KILL);
      abilityEvents.add(techDrop);
      return abilityEvents;
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
