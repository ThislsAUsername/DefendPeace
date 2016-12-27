package Engine.Combat;

import Terrain.GameMap;
import Units.Unit;

public class CombatParameters
{
  public Unit attacker, defender;
  public double baseDamage, attackFactor, attackerHP, defenseFactor, defenderHP, terrainDefenseLevel;
  public GameMap map;
  public boolean isCounter, canCounter;

  public CombatParameters(Unit pAttacker, Unit pDefender, GameMap pMap)
  {
    attacker = pAttacker;
    defender = pDefender;
    map = pMap;
    isCounter = false;
    // Only attacks at point-blank can be countered
    int dist = Math.abs(attacker.x - defender.x) + Math.abs(attacker.y - defender.y);
    canCounter = !isCounter && dist == 1 && defender.getDamage(attacker) != 0;
    calculateParameters();
  }

  public double calculateDamage()
  {
    //		[B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
    double overallPower = (baseDamage * attackFactor / 100/*+Random factor?*/) * attackerHP / 10;
    double overallDefense = ((200 - (defenseFactor + terrainDefenseLevel * defenderHP)) / 100);
    return overallPower * overallDefense / 10; // original formula was % damage, now it must be HP of damage
  }

  /**
   * Makes the attacker the defender, inverts the counter flag, and recalculates the rest of the parameters.
   */
  public void swap()
  {
    if( !canCounter )
    {
      System.out.println("Error in CombatParameters.Swap()! Attack is noted as being uncounterable, but swapping is happening.");
    }
    Unit temp = attacker;
    attacker = defender;
    defender = temp;
    isCounter = !isCounter;
    calculateParameters();
  }

  private void calculateParameters()
  {
    baseDamage = attacker.getDamage(defender);
    attackFactor = attacker.model.getDamageRatio();
    attackerHP = attacker.getHP();
    defenseFactor = defender.model.getDefenseRatio();
    defenderHP = defender.getHP();
    terrainDefenseLevel = map.getEnvironment(defender.x, defender.y).getDefLevel();
  }
}
