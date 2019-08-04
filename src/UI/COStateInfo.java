package UI;

import CommandingOfficers.Commander;
import Terrain.GameMap;
import Terrain.Location;
import Units.Unit;
import Units.UnitModel.ChassisEnum;

/** Convenience class to collate and format visible game state info for a given CO */
public class COStateInfo // TODO: Consider making this class parse data for all COs at once
{
  int income = 0;
  int unitCount = 0;
  int vehCount = 0;
  int unitFunds = 0;

  Boolean showAbilityInfo = false;
  double abilityPower = 0;
  double untilNextPower = 0;

  public COStateInfo(GameMap map, Commander viewed)
  {
    // map-based info
    for( int w = 0; w < map.mapWidth; ++w )
    {
      for( int h = 0; h < map.mapHeight; ++h )
      {
        Location loc = map.getLocation(w, h);
        if( loc.isProfitable() && loc.getOwner() == viewed )
        {
          income += viewed.gameRules.incomePerCity + viewed.incomeAdjustment;
        }
        Unit resident = loc.getResident();
        if( null != resident && resident.CO == viewed )
        {
          unitCount++;
          if( resident.model.chassis != ChassisEnum.TROOP )
            vehCount++;
          unitFunds += resident.model.getCost() * resident.getHP() / resident.model.maxHP;
        }
      }
    }

    // ability stats
    double[] abilityCosts = viewed.getAbilityCosts();
    if( abilityCosts.length > 0 )
    {
      showAbilityInfo = true;
      abilityPower = viewed.getAbilityPower();

      for( double cost : abilityCosts ) // init to our biggest cost, so we can only go down
        untilNextPower = Math.max(untilNextPower, cost);

      for( double cost : abilityCosts ) // find the cheapest cost that we can't afford
      {
        if( cost >= abilityPower )
          untilNextPower = Math.min(untilNextPower, cost - abilityPower);
      }
    }
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
  public String getFullStatus()
  {
    StringBuilder sb = new StringBuilder();

    sb.append("Income:         ").append(income).append("\n");
    sb.append("Unit count:     ").append(unitCount).append("\n");
    sb.append("Vehicle count:  ").append(vehCount).append("\n");
    sb.append("Unit funds:     ").append(unitFunds).append("\n");
    if( showAbilityInfo )
    {
      sb.append("Ability Power:  ").append((int) (abilityPower * Commander.CHARGERATIO_FUNDS)).append("\n");
      sb.append("Next Ability:   ").append((int) (untilNextPower * Commander.CHARGERATIO_FUNDS)).append("\n");
    }

    return sb.toString();
  }
}
