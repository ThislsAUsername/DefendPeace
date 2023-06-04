package UI;

import java.util.HashMap;

import CommandingOfficers.Commander;
import Engine.Army;
import Terrain.GameMap;
import Terrain.MapLocation;
import Units.Unit;

/** Convenience class to collate and format visible game state info for a given CO */
public class COStateInfo // TODO: Consider making this class parse data for all COs at once
{
  String identifier = "";
  int income = 0;
  int unitCount = 0;
  int vehCount = 0;
  int unitFunds = 0;

  HashMap<Commander, Integer> currentEnergy = new HashMap<>();
  HashMap<Commander, Integer> untilNextEnergy = new HashMap<>();

  public COStateInfo(GameMap map, Army thisArmy)
  {
    identifier = thisArmy.cos[0].coInfo.name;
    // Start at 1 so we don't double-add the first one
    for( int i = 1; i < thisArmy.cos.length; ++i )
      identifier += ", " + thisArmy.cos[i].coInfo.name;

    // map-based info
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        MapLocation loc = map.getLocation(w, h);
        final Commander owner = loc.getOwner();
        if( loc.isProfitable() && owner != null && owner.army == thisArmy )
        {
          income += owner.gameRules.incomePerCity + owner.incomeAdjustment;
        }
        Unit resident = loc.getResident();
        if( null != resident && resident.CO.army == thisArmy )
        {
          unitCount++;
          if( resident.model.isTroop() )
            vehCount++;
          unitFunds += resident.getCost() * resident.getHPFactor();
        }
      }
    }

    // ability stats
    for( Commander co : thisArmy.cos )
    {
      int[] abilityCosts = co.getAbilityCosts();
      if( abilityCosts.length > 0 )
      {
        currentEnergy.put(co, (co.getAbilityPower()));
        untilNextEnergy.put(co, getEnergyUntilNextPower(co));
      }
    }
  }

  public static int getEnergyUntilNextPower(Commander viewed)
  {
    int output = 0;
    int[] abilityCosts = viewed.getAbilityCosts();
    if( abilityCosts.length > 0 )
    {
      int abilityPower = viewed.getAbilityPower();
      for( int cost : abilityCosts ) // init to our biggest cost, so we can only go down
        output = Math.max(output, cost);

      for( int cost : abilityCosts ) // find the cheapest cost that we can't afford
      {
        if( cost >= abilityPower )
          output = Math.min(output, cost - abilityPower);
      }
    }
    return (output);
  }

  /** Returns a string with just the income and unit count of a given CO */
  public String getAbbrevStatus()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("Income: ").append(income/1000).append("k  ");
    sb.append("Unit count: ").append(unitCount);
    return sb.toString();
  }

  /** Returns a string describing the game statistics of a given CO */
  public String getFullStatusValues()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("\n");
    sb.append(income)   .append("\n");
    sb.append(unitCount).append("\n");
    sb.append(vehCount) .append("\n");
    sb.append(unitFunds).append("\n");
    for( Commander co : currentEnergy.keySet() )
    {
      sb.append(currentEnergy.get(co))  .append("\n");
      sb.append(untilNextEnergy.get(co)).append("\n");
    }

    return sb.toString();
  }
  public String getFullStatusLabels()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(identifier)      .append("\n");
    sb.append("Income:")       .append("\n");
    sb.append("Unit count:")   .append("\n");
    sb.append("Vehicle count:").append("\n");
    sb.append("Unit funds:")   .append("\n");
    for( Commander co : currentEnergy.keySet() )
    {
      sb.append(co.coInfo.name + " Ability:").append("\n");
      sb.append(" - Next Ability:") .append("\n");
    }

    return sb.toString();
  }
}
