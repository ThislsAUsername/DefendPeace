package Engine.Combat;

import Terrain.Environment;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.Weapon;

public class BattleInstance
{
  private Unit attacker, defender;
  private Weapon attackerWeapon = null, defenderWeapon = null;
  private final int attackerX, attackerY, defenderX, defenderY;
  private GameMap gameMap;
  private boolean canCounter = false;
  private boolean attackerMoved;
  int battleRange;

  /**
   * Set up the CombatParams object. Note that we assume the defender is where he thinks he is, but
   * the attacker may move before attacking, so we take that coordinate explicitly.
   * @param pAttacker
   * @param pDefender
   * @param map
   * @param attackerX
   * @param attackerY
   */
  public BattleInstance(Unit pAttacker, Unit pDefender, GameMap map, int attackerX, int attackerY)
  {
    attacker = pAttacker;
    defender = pDefender;
    this.attackerX = attackerX;
    this.attackerY = attackerY;
    defenderX = defender.x; // This variable is technically not necessary, but provides consistent names with attackerX/Y.
    defenderY = defender.y;
    gameMap = map;
    attackerMoved = pAttacker.x != attackerX || pAttacker.y != attackerY;
    battleRange = Math.abs(attackerX - defenderX) + Math.abs(attackerY - defenderY);
    attackerWeapon = attacker.chooseWeapon(defender.model, battleRange, attackerMoved);
    // Only attacks at point-blank range can be countered
    if( 1 == battleRange )
    {
      defenderWeapon = defender.chooseWeapon(attacker.model, battleRange, false);
      if( null != defenderWeapon )
      {
        canCounter = true;
      }
    }
  }

  /**
   * Calculate and return the results of this battle.
   * NOTE: This will not actually apply the damage taken; this is done later in BattleEvent.
   * @return A BattleSummary object containing all relevant details from this combat instance.
   */
  public BattleSummary calculateBattleResults()
  {
    double attackerHPLoss = 0;

    // Set up our scenario.
    BattleParams attackInstance = new BattleParams(attacker, attackerWeapon, defender, gameMap.getEnvironment(defenderX,
        defenderY), false, battleRange);

    // Last-minute adjustments.
    attacker.CO.applyCombatModifiers(attackInstance,gameMap);
    defender.CO.applyCombatModifiers(attackInstance,gameMap);

    double defenderHPLoss = attackInstance.calculateDamage();
    if( defenderHPLoss > defender.getPreciseHP() ) defenderHPLoss = defender.getPreciseHP();

    // If the unit can counter, and wasn't killed in the initial volley, calculate return damage.
    if( canCounter && (defender.getHP() > defenderHPLoss) )
    {
      // New battle instance with defender counter-attacking.
      BattleParams defendInstance = new BattleParams(defender, defenderWeapon, attacker, gameMap.getEnvironment(attackerX,
          attackerY), true, battleRange);
      defendInstance.attackerHP -= defenderHPLoss; // Account for the first attack's damage to the now-attacker.

      attacker.CO.applyCombatModifiers(attackInstance,gameMap);
      defender.CO.applyCombatModifiers(attackInstance,gameMap);

      attackerHPLoss = defendInstance.calculateDamage();
      if( attackerHPLoss > attacker.getPreciseHP() ) attackerHPLoss = attacker.getPreciseHP();
    }

    // Build and return the BattleSummary.
    return new BattleSummary(attacker, attackerWeapon, defender, defenderWeapon, gameMap.getEnvironment(attackerX, attackerY).terrainType,
        gameMap.getEnvironment(defenderX, defenderY).terrainType, attackerHPLoss, defenderHPLoss, attackInstance.luckDamage);
  }

  /**
   * Utility struct used to facilitate calculating battle results.
   */
  public class BattleParams
  {
    public final Unit attacker, defender;
    public double baseDamage;
    public double attackerHP;
    public double attackFactor;
    public double defenderHP;
    public double defenseFactor;
    public double terrainDefense;
    public boolean isCounter;
    public double luckDamage;
    public int range;

    public BattleParams(Unit attacker, Weapon attackerWeapon, Unit defender, Environment battleground, boolean isCounter, int range)
    {
      this.attacker = attacker;
      this.defender = defender;
      this.isCounter = isCounter;
      this.range = range;
      baseDamage = attackerWeapon.getDamage(defender.model);
      attackFactor = attacker.model.getDamageRatio();
      attackerHP = attacker.getHP();
      defenseFactor = defender.model.getDefenseRatio();
      defenderHP = defender.getHP();
      terrainDefense = 0;
      // Air units shouldn't get terrain defense
      if( ChassisEnum.AIR_HIGH != defender.model.chassis && ChassisEnum.AIR_LOW != defender.model.chassis )
      {
        // getDefLevel returns the number of terrain stars. Since we're using %Def, we need to multiply by 10. However, we do that when we multiply by HP in calculateDamage.
        terrainDefense = battleground.terrainType.getDefLevel();
      }
    }

    public double calculateDamage()
    {
      //    [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
      double overallPower = (baseDamage * attackFactor / 100) * attackerHP / 10;
      double overallDefense = ((200 - (defenseFactor + terrainDefense * defenderHP)) / 100);
      luckDamage = (int)(Math.random()*10) * (attackerHP / 10) * (overallDefense / 10);
      return overallPower * overallDefense / 10 + luckDamage; // original formula was % damage, now it must be HP of damage
    }
  }
}
