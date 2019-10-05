package Engine.Combat;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import Terrain.Environment;
import Terrain.GameMap;
import Units.Unit;
import Units.UnitModel.ChassisEnum;
import Units.Weapons.Weapon;

/**
 * BattleInstance provides COs with two levels of control of what goes on in combat:
 * changeCombatContext(), which takes in a CombatContext and allows the COs to fiddle with just about anything.
 * applyCombatModifiers(), which is for directly modifying a unit's firepower in a given attack.
 */
public class BattleInstance
{
  private final Unit attacker, defender;
  private final Weapon attackerWeapon, defenderWeapon;
  private final int attackerX, attackerY, defenderX, defenderY;
  private final GameMap gameMap;
  private final boolean attackerMoved;
  /** Determines whether to cap damage at the HP of the victim in question */
  private final boolean isSim;
  private final int battleRange;

  /**
   * Set up the CombatParams object. Note that we assume the defender is where he thinks he is, but
   * the attacker may move before attacking, so we take that coordinate explicitly.
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
    }
    else
    {
      defenderWeapon = null;
    }
  }

  /**
   * Calculate and return the results of this battle.
   * NOTE: This will not actually apply the damage taken; this is done later in BattleEvent.
   * @return A BattleSummary object containing all relevant details from this combat instance.
   */
  public BattleSummary calculateBattleResults()
  {
    CombatContext context = new CombatContext(gameMap, attacker, attackerWeapon, defender, defenderWeapon, battleRange, attackerX, attackerY);
    
    // If the attacker and defender get swapped, we want to still give a coherent BattleSummary.
    Map<Unit, Entry<Weapon,Double>> unitDamageMap = new HashMap<Unit, Entry<Weapon,Double>>();
    unitDamageMap.put(attacker, new AbstractMap.SimpleEntry<Weapon,Double>(attackerWeapon, 0.0));
    unitDamageMap.put(defender, new AbstractMap.SimpleEntry<Weapon,Double>(defenderWeapon, 0.0));
    
    // let the COs fool around with anything they want...
    attacker.CO.changeCombatContext(context);
    defender.CO.changeCombatContext(context);
    
    // From here on in, use context variables only (Easy way to confirm: delete BattleInstance's variable block and see what gets marked as an error)
    
    double attackerHPLoss = 0;

    // Set up our scenario.
    BattleParams attackInstance = BattleParams.getAttack(context);

    // Last-minute adjustments.
    context.attacker.CO.applyCombatModifiers(attackInstance, true);
    context.defender.CO.applyCombatModifiers(attackInstance, false);

    double defenderHPLoss = attackInstance.calculateDamage(isSim);
    unitDamageMap.put(context.attacker, new AbstractMap.SimpleEntry<Weapon,Double>(context.attackerWeapon, defenderHPLoss));
    if( !isSim && defenderHPLoss > context.defender.getPreciseHP() ) defenderHPLoss = context.defender.getPreciseHP();

    // If the unit can counter, and wasn't killed in the initial volley, calculate return damage.
    if( context.canCounter && (context.defender.getPreciseHP() > defenderHPLoss) )
    {
      // New battle instance with defender counter-attacking.
      BattleParams defendInstance = BattleParams.getCounterAttack(context);
      defendInstance.attackerHP = Math.ceil(context.defender.getPreciseHP() - defenderHPLoss); // Account for the first attack's damage to the now-attacker.

      // Modifications apply "attacker first", and the defender is now the attacker.
      context.defender.CO.applyCombatModifiers(defendInstance, true);
      context.attacker.CO.applyCombatModifiers(defendInstance, false);

      attackerHPLoss = defendInstance.calculateDamage(isSim);
      unitDamageMap.put(context.defender, new AbstractMap.SimpleEntry<Weapon,Double>(context.defenderWeapon, attackerHPLoss));
      if( !isSim && attackerHPLoss > context.attacker.getPreciseHP() ) attackerHPLoss = context.attacker.getPreciseHP();
    }
    
    // Calculations complete. We are setting up our BattleSummary, so go back to using known-accurate info
    return new BattleSummary(attacker, unitDamageMap.get(attacker).getKey(),
                             defender, unitDamageMap.get(defender).getKey(), 
                             gameMap.getEnvironment(attackerX, attackerY).terrainType,
                             gameMap.getEnvironment(defenderX, defenderY).terrainType, 
                             unitDamageMap.get(defender).getValue(), unitDamageMap.get(attacker).getValue());
  }
  
  /**
   * CombatContext exists to allow COs to modify any crazy thing they want about a combat, without the BattleInstance itself mutating.
   */
  public static class CombatContext
  {
    public Unit attacker, defender;
    public Weapon attackerWeapon = null, defenderWeapon = null;
    public int attackerX, attackerY, defenderX, defenderY;
    public final GameMap gameMap; // for reference, not weirdness
    public boolean canCounter = false;
    public boolean attackerMoved;
    public int battleRange;
    
    public CombatContext(GameMap map, Unit pAttacker, Weapon attackerWep, Unit pDefender, Weapon defenderWep, int pBattleRange, int attackerX, int attackerY)
    {
      attacker = pAttacker;
      defender = pDefender;
      this.attackerX = attackerX;
      this.attackerY = attackerY;
      defenderX = defender.x; // This variable is technically not necessary, but provides consistent names with attackerX/Y.
      defenderY = defender.y;
      gameMap = map;
      attackerMoved = pAttacker.x != attackerX || pAttacker.y != attackerY;
      battleRange = pBattleRange;
      attackerWeapon = attackerWep;
      defenderWeapon = defenderWep;
      if( null != defenderWeapon )
      {
        canCounter = true;
      }
    }
  }

  /**
   * Utility struct used to facilitate calculating battle results.
   * Parameters are public to allow modification in Commander.applyCombatModifiers()
   * One BattleParams is a single attack from a single unit; any counterattack is a second instance. 
   */
  public static class BattleParams
  {
    public final Unit attacker, defender; 
    public final CombatContext combatRef; // strictly for reference
    public double baseDamage;
    public double attackerHP;
    public double attackFactor;
    public double defenderHP;
    public double defenseFactor;
    public double terrainDefense;
    public final boolean isCounter;
    public double luckMax = 10;
    public double dispersion = 0;

    public static BattleParams getAttack(final CombatContext ref)
    {
      return new BattleParams(ref.attacker, ref.attackerWeapon, ref.defender, ref.gameMap.getEnvironment(ref.defenderX, ref.defenderY), false, ref);
    }
    public static BattleParams getCounterAttack(final CombatContext ref)
    {
      return new BattleParams(ref.defender, ref.defenderWeapon, ref.attacker, ref.gameMap.getEnvironment(ref.attackerX, ref.attackerY), true, ref);
    }

    public BattleParams(Unit attacker, Weapon attackerWeapon, Unit defender, Environment battleground, boolean isCounter, final CombatContext ref)
    {
      this.attacker = attacker;
      this.defender = defender;
      baseDamage = attackerWeapon.getDamage(defender.model);
      attackFactor = attacker.model.getDamageRatio() + attacker.CO.getTowerBoost();
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

    public double calculateDamage(boolean isSim)
    {
      //    [B*ACO/100+R]*(AHP/10)*[(200-(DCO+DTR*DHP))/100]
      double overallPower = (baseDamage * attackFactor / 100) * attackerHP / 10;
      double overallDefense = ((200 - (defenseFactor + terrainDefense * defenderHP)) / 100);
      double luckDamage = 0;
      if( ! isSim )
      {
        luckDamage = (int) (Math.random() * luckMax) * (attackerHP / 10);
        luckDamage -= (int) (Math.random() * dispersion) * (attackerHP / 10);
      }
      return (overallPower + luckDamage) * overallDefense / 10 ; // original formula was % damage, now it must be HP of damage
    }
  }
}
