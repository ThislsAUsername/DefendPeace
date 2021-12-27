package UI;

import CommandingOfficers.Commander;
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

  Boolean showAbilityInfo = false;
  int abilityPower = 0;
  int untilNextPower = 0;

  public COStateInfo(GameMap map, Commander viewed)
  {
    identifier = viewed.coInfo.name;

    // map-based info
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        MapLocation loc = map.getLocation(w, h);
        if( loc.isProfitable() && loc.getOwner() == viewed )
        {
          income += viewed.gameRules.incomePerCity + viewed.incomeAdjustment;
        }
        Unit resident = loc.getResident();
        if( null != resident && resident.CO == viewed )
        {
          unitCount++;
          if( resident.model.isTroop() )
            vehCount++;
          unitFunds += resident.getCost() * resident.getHPFactor();
        }
      }
    }

    // ability stats
    double[] abilityCosts = viewed.getAbilityCosts();
    if( abilityCosts.length > 0 )
    {
      showAbilityInfo = true;
      abilityPower = energyToFunds(viewed.getAbilityPower());
      untilNextPower = getEnergyUntilNextPower(viewed);
    }
  }

  public static int getEnergyUntilNextPower(Commander viewed)
  {
    double output = 0;
    double[] abilityCosts = viewed.getAbilityCosts();
    if( abilityCosts.length > 0 )
    {
      double abilityPower = viewed.getAbilityPower();
      for( double cost : abilityCosts ) // init to our biggest cost, so we can only go down
        output = Math.max(output, cost);

      for( double cost : abilityCosts ) // find the cheapest cost that we can't afford
      {
        if( cost >= abilityPower )
          output = Math.min(output, cost - abilityPower);
      }
    }
    return energyToFunds(output);
  }

  public static int energyToFunds(double energy)
  {
    return (int) (energy * Commander.CHARGERATIO_FUNDS);
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
    if( showAbilityInfo )
    {
      sb.append(abilityPower)  .append("\n");
      sb.append(untilNextPower).append("\n");
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
    if( showAbilityInfo )
    {
      sb.append("Ability Power:").append("\n");
      sb.append("Next Ability:") .append("\n");
    }

    return sb.toString();
  }
}
