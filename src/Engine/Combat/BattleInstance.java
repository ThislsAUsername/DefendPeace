package Engine.Combat;

import Terrain.Environment;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.Weapon;

/**
 * BattleInstance provides COs with two levels of control of what goes on in combat:
 * changeCombatContext(), which takes in the BattleInstance itself and allows the COs to fiddle with just about anything.
 * applyCombatModifiers(), which is for directly modifying a unit's firepower in a given attack.
 * All variables are public so that the COs can actually modify them.
 */
public class BattleInstance
{
  public Unit attacker, defender;
  public Weapon attackerWeapon = null, defenderWeapon = null;
  public int attackerX, attackerY, defenderX, defenderY;
  public final GameMap gameMap; // for reference, not weirdness
  public boolean canCounter = false;
  public boolean attackerMoved;
  /** Determines whether to cap damage at the HP of the victim in question */
  public final boolean isSim;
  int battleRange;

  /**
   * Set up the CombatParams object. Note that we assume the defender is where he thinks he is, but
   * the attacker may move before attacking, so we take that coordinate explicitly.
   * @param isSim 
   */
  public BattleInstance(Unit pAttacker, Unit pDefender, GameMap map, int attackerX, int attackerY, boolean isSim)
  {
    this.isSim = isSim;
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
    // let the COs fool around with anything they want...
    attacker.CO.changeCombatContext(this);
    defender.CO.changeCombatContext(this);
    
    double attackerHPLoss = 0;

    // Set up our scenario.
    BattleParams attackInstance = new BattleParams(attacker, attackerWeapon,
        defender, gameMap.getEnvironment(defenderX, defenderY), false, this);

    // Last-minute adjustments.
    attacker.CO.applyCombatModifiers(attackInstance, true);
    defender.CO.applyCombatModifiers(attackInstance, false);

    double defenderHPLoss = attackInstance.calculateDamage();
    if( !isSim && defenderHPLoss > defender.getPreciseHP() ) defenderHPLoss = defender.getPreciseHP();

    // If the unit can counter, and wasn't killed in the initial volley, calculate return damage.
    if( canCounter && (defender.getPreciseHP() > defenderHPLoss) )
    {
      // New battle instance with defender counter-attacking.
      BattleParams defendInstance = new BattleParams(defender, defenderWeapon,
          attacker, gameMap.getEnvironment(attackerX, attackerY), true, this);
      defendInstance.attackerHP -= defenderHPLoss; // Account for the first attack's damage to the now-attacker.

      // Modifications apply "attacker first", and the defender is now the attacker.
      defender.CO.applyCombatModifiers(attackInstance, true);
      attacker.CO.applyCombatModifiers(attackInstance, false);

      attackerHPLoss = defendInstance.calculateDamage();
      if( !isSim && attackerHPLoss > attacker.getPreciseHP() ) attackerHPLoss = attacker.getPreciseHP();
    }

    // Build and return the BattleSummary.
    return new BattleSummary(attacker, attackerWeapon, defender, defenderWeapon, gameMap.getEnvironment(attackerX, attackerY).terrainType,
        gameMap.getEnvironment(defenderX, defenderY).terrainType, attackerHPLoss, defenderHPLoss);
  }

  /**
   * Utility struct used to facilitate calculating battle results.
   * Parameters are public to allow modification in Commander.applyCombatModifiers()
   * One BattleParams is a single attack from a single unit; any counterattack is a second instance. 
   */
  public static class BattleParams
  {
    public final Unit attacker, defender; 
    public final BattleInstance combatRef; // strictly for reference
    public double baseDamage;
    public double attackerHP;
    public double attackFactor;
    public double defenderHP;
    public double defenseFactor;
    public double terrainDefense;
    public final boolean isCounter;

    public BattleParams(Unit attacker, Weapon attackerWeapon, Unit defender, Environment battleground, boolean isCounter, final BattleInstance ref)
    {
      this.attacker = attacker;
      this.defender = defender;
      baseDamage = attackerWeapon.getDamage(defender.model);
      attackFactor = attacker.model.getDamageRatio();
      attackerHP = attacker.getHP();
      defenseFactor = defender.model.getDefenseRatio();
      defenderHP = defender.getHP();
      terrainDefense = 0;
      this.isCounter = isCounter;
      combatRef = ref;
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
      double overallPower = (baseDamage * attackFactor / 100/*+Random factor?*/) * attackerHP / 10;
      double overallDefense = ((200 - (defenseFactor + terrainDefense * defenderHP)) / 100);
      return overallPower * overallDefense / 10; // original formula was % damage, now it must be HP of damage
    }
  }
}
