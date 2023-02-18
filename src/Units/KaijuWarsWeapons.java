package Units;

import java.util.HashSet;

import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.StateTrackers.StateTracker;
import Engine.StateTrackers.UnitTurnPositionTracker;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.GameMap;
import Terrain.TerrainType;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;

public class KaijuWarsWeapons
{
  public final static int SLOW_BONUS          = 2;
  public final static int RESIST_KAIJU_BONUS  = 2;
  public final static int STATIC_INF_BONUS    = 2;
  public final static int DIVINE_WIND_BONUS   = 2;
  public final static int RANGEFINDERS_BONUS  = 2;
  public final static int STATIC_ROCKET_BONUS = 2;

  public final static int TERRAIN_DURABILITY  = 4;

  private static class KaijuWarsWeapon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private final static boolean infiniteAmmo = true;

    public boolean isAirWeapon          = false;
    public boolean negateCounterBonuses = false;
    public final int vsLand, vsAir;
    protected KaijuWarsWeapon(int vsLand, int vsAir, int minRange, int maxRange)
    {
      super(infiniteAmmo, minRange, maxRange);
      this.vsLand = vsLand;
      this.vsAir  = vsAir;
    }
    protected KaijuWarsWeapon(int vsLand, int vsAir)
    {
      this(vsLand, vsAir, 1, 1);
    }
    public KaijuWarsWeapon(KaijuWarsWeapon other)
    {
      this(other.vsLand, other.vsAir, other.rangeMin, other.rangeMax);
      canFireAfterMoving   = other.canFireAfterMoving;
      isAirWeapon          = other.isAirWeapon;
      negateCounterBonuses = other.negateCounterBonuses;
    }
    @Override
    public WeaponModel clone()
    {
      return new KaijuWarsWeapon(this);
    }

    @Override
    public double getDamage(KaijuWarsUnitModel defender)
    {
      int attack = deriveAttack(this, defender);
      if( defender.isKaiju )
        return attack * 10;

      int counterPower = defender.kaijuCounter;

      return KaijuWarsWeapons.getDamage(attack, 0, counterPower);
    }

    @Override
    public double getDamage(TerrainType target)
    {
      if( TerrainType.METEOR == target )
        return KaijuWarsWeapons.getDamage(vsLand, 0, TERRAIN_DURABILITY);
      return 0;
    }
  }

  // generic land
  public static class ShootLand extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public ShootLand(int power)
    {
      super(power, 0);
    }
  }
  public static class ShootAll extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public ShootAll(int power)
    {
      super(power, power);
    }
  }

  // specific land
  public static class Missile extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public Missile()
    {
      super(2, 3);
      negateCounterBonuses = true;
    }
  }
  // Missiles are really bad, so these get to be "buffed Missile" because just giving them this buff would be busted
  public static class Rockets extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 2;

    public Rockets()
    {
      super(2, 3, MIN_RANGE, MAX_RANGE);
      canFireAfterMoving = true;
      negateCounterBonuses = true;
    }
  }
  public static class AA extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public AA()
    {
      super(1, 2);
    }
  }
  public static class Artillery extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 3;

    public Artillery()
    {
      super(1, 0, MIN_RANGE, MAX_RANGE);
      negateCounterBonuses = true;
    }
  }

  // air
  public static class Fighter extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public Fighter()
    {
      super(1, 1);
      isAirWeapon = true;
    }
  }
  public static class Bomber extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public Bomber()
    {
      super(2, 0);
      isAirWeapon = true;
    }
  }

  // experimental weapons

  public static class Maser extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public Maser()
    {
      super(6, 0);
      negateCounterBonuses = true;
    }
  }

  // This will need to penalize movement next turn by 1, if it's ever implemented.
  public static class Freezer extends ShootAll
  {
    private static final long serialVersionUID = 1L;

    public Freezer()
    {
      super(1);
    }
  }
  public static class CannonOfBoom extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 5;

    public CannonOfBoom()
    {
      super(3, 2, MIN_RANGE, MAX_RANGE);
      negateCounterBonuses = true;
    }
  }
  public static class GuncrossWing extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public GuncrossWing()
    {
      super(2, 3);
      isAirWeapon = true;
    }
  }
  public static class GuncrossRobot extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public GuncrossRobot()
    {
      super(4, 2);
    }
  }
  public static class SuperZ2 extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public SuperZ2()
    {
      super(3, 2);
    }
  }
  public static class BigBoy extends KaijuWarsWeapon
  {
    private static final long serialVersionUID = 1L;

    public BigBoy()
    {
      super(4, 0);
      isAirWeapon = true;
    }
  }
  public static class Kaputnik extends ShootAll
  {
    private static final long serialVersionUID = 1L;

    public Kaputnik()
    {
      super(8);
      isAirWeapon = true;
    }
  }
  public static class SharkJet extends ShootAll
  {
    private static final long serialVersionUID = 1L;

    public SharkJet()
    {
      super(1);
      isAirWeapon = true;
    }
  }

  // Detailed damage calcs

  public static int deriveAttack(KaijuWarsWeapon gun, KaijuWarsUnitModel defender)
  {
    int attack = 0;
    if( defender.isAirUnit() )
      attack = gun.vsAir;
    else
      attack = gun.vsLand;
    return attack;
  }

  public static double getDamage(int attack, int attBonus, int counterPower)
  {
    return getDamageRatioStyle(attack, attBonus, counterPower);
  }

  // Percent damage that 1 ATK should do vs 0 kaijuCounter
  public final static int KAIJU_DAMAGE_FACTOR = 80;
  /**
   * Produces damage numbers based on attack/kaijuCounter
   */
  public static double getDamageRatioStyle(int attack, int attBonus, int counterPower)
  {
    // 1-based instead of 0-based
    int durability = 1 + counterPower;
    int damage = attack * KAIJU_DAMAGE_FACTOR / durability;
    damage    -= damage % 5;
    damage    += attBonus * 20;

    return damage;
  }

  /**
   * Produces damage numbers like AW's?
   */
  public static double getDamageShiftingStyle(int attack, int attBonus, int durability)
  {
    if( 0 == attack )
      return 0;

    int damage = 45 + (attack + attBonus)*20;

    int finalPower = attack - durability;
    if( finalPower > 0 )
      damage += finalPower * 20;
    else
      damage += finalPower * 15;

    return damage;
  }

  // Implements all +ATK and +counter as base damage changes
  public static class KaijuWarsFightMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void changeCombatContext(CombatContext instance)
    {
      KaijuWarsWeapon gun = (KaijuWarsWeapon) instance.attacker.weapon;
      if( gun.negateCounterBonuses )
      {
        instance.canCounter = false;
      }
    }

    // Rewrite base damage in both of these functions so we can beat up both terrain and units with situational boosts
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      KaijuWarsWeapon gun = (KaijuWarsWeapon) params.attacker.weapon;
      int attack = gun.vsLand;

      final TerrainType atkEnv = params.attacker.env.terrainType;
      int attackBoost = getAttackBoost(params.attacker.unit, params.map, params.attacker.coord, atkEnv, params.targetCoord);

      // Assume we're shooting terrain, since the base damage will get overwritten later
      params.baseDamage = getDamage(attack, attackBoost, TERRAIN_DURABILITY);
    }

    @Override
    public void modifyUnitAttackOnUnit(BattleParams params)
    {
      KaijuWarsWeapon gun = (KaijuWarsWeapon) params.attacker.weapon;
      KaijuWarsUnitModel defModel = (KaijuWarsUnitModel) params.defender.model;
      final TerrainType atkEnv = params.attacker.env.terrainType;
      final TerrainType defEnv = params.defender.env.terrainType;

      int counterBoost = getCounterBoost(params.defender, params.map, defEnv);
      int counterPower = defModel.kaijuCounter;
      counterPower += counterBoost;

      int attackBoost = getAttackBoost(params.attacker.unit, params.map, params.attacker.coord, atkEnv, params.defender.coord);
      int attack = deriveAttack(gun, defModel);

      if( defModel.isKaiju )
      {
        params.terrainStars = 0;
        params.baseDamage = (attack + attackBoost) * 10;
      }
      else
        params.baseDamage = getDamage(attack, attackBoost, counterPower);
    }

    // Throwing the air-airport move boost on here since this modifier's going on all units anyway
    @Override
    public void modifyMovePower(UnitContext uc)
    {
      if( uc.map == null )
        return;
      if( !uc.map.isLocationValid(uc.coord) )
        return;
      if( !uc.model.isAirUnit() )
        return;
      TerrainType tt = uc.map.getEnvironment(uc.coord).terrainType;
      if( tt.healsAir() )
        uc.movePower += 3;
    }
  } //~KaijuWarsFightMod

  /**
   * Gets any situational counter power boost from unit mechanics or assumed-enabled tactics/techs
   */
  public static int getCounterBoost(UnitContext defender, GameMap map, TerrainType defEnv)
  {
    int counterBoost = 0;
    KaijuWarsUnitModel defModel = (KaijuWarsUnitModel) defender.model;
    if( defModel.entrenches )
    {
      // This is a bit smelly, but better than the alternative?
      UnitTurnPositionTracker tracker = StateTracker.instance(map.game, UnitTurnPositionTracker.class);
      if( tracker.stoodStill(defender) )
        counterBoost += STATIC_INF_BONUS;
    }
    if( defModel.divineWind &&
        (defEnv == TerrainType.GRASS || defEnv == TerrainType.SEA) )
      counterBoost += DIVINE_WIND_BONUS;
    return counterBoost;
  }
  /**
   * Gets any situational attack power boost from unit mechanics or assumed-enabled tactics/techs
   */
  public static int getAttackBoost(Unit attacker, GameMap map, XYCoord atkCoord, TerrainType atkEnv, XYCoord targetCoord)
  {
    KaijuWarsUnitModel atkModel = (KaijuWarsUnitModel) attacker.model;
    int attackBoost = 0;
    // Rangefinders boost - these units are pretty stally, so why not?
    if( attacker.model.isLandUnit() &&
        atkEnv == TerrainType.MOUNTAIN )
      attackBoost += RANGEFINDERS_BONUS;
    // Missiles boost
    if( atkModel.stillBoost )
    {
      // This is a bit smelly, but better than the alternative?
      UnitTurnPositionTracker tracker = StateTracker.instance(map.game, UnitTurnPositionTracker.class);
      if( tracker.stoodStill(attacker, atkCoord) )
        attackBoost += STATIC_ROCKET_BONUS;
    }
    // Apply copter/Sky Carrier adjacency boost
    for( XYCoord xyc : Utils.findLocationsInRange(map, atkCoord, 1, 1) )
    {
      Unit resident = map.getResident(xyc);
      if( null != resident && resident != attacker && !attacker.CO.isEnemy(resident.CO) )
      {
        KaijuWarsUnitModel resModel = (KaijuWarsUnitModel) resident.model;
        if( null != resModel && resModel.boostsAllies )
          attackBoost += 1;
      }
    }
    // Apply AA surround boost
    for( XYCoord xyc : Utils.findLocationsInRange(map, targetCoord, 1, 1) )
    {
      Unit resident = map.getResident(xyc);
      if( null != resident && resident != attacker && !attacker.CO.isEnemy(resident.CO) )
      {
        KaijuWarsUnitModel resModel = (KaijuWarsUnitModel) resident.model;
        if( null != resModel && resModel.boostSurround )
          attackBoost += 1;
      }
    }
    return attackBoost;
  }


  /**
   * Applies stuns in place of defensive slows, in military vs military fights
   */
  public static class CombatStunApplier extends StateTracker
  {
    private static final long serialVersionUID = 1L;

    @Override
    public GameEventQueue receiveBattleEvent(BattleSummary summary)
    {
      KaijuWarsUnitModel atkModel = (KaijuWarsUnitModel) summary.attacker.model;
      // TODO: Does this do bad things with BattleMechs?
      KaijuWarsWeapon    atkGun = (KaijuWarsWeapon) atkModel.weapons.get(0);
      KaijuWarsUnitModel defModel = (KaijuWarsUnitModel) summary.defender.model;
      KaijuWarsWeapon defGun = null;
      if( !defModel.weapons.isEmpty() )
        defGun = (KaijuWarsWeapon) defModel.weapons.get(0);
      else
        defGun = new KaijuWarsWeapon(0, 0);

      // Store whether said combatant should be immune to stun
      boolean stunAttacker = !atkGun.negateCounterBonuses && !atkModel.resistsKaiju;
      boolean stunDefender = !defGun.negateCounterBonuses && !defModel.resistsKaiju;

      // If not immune, check whether the other one can stun this one
      if( stunAttacker )
        stunAttacker = canStunType(atkGun, defModel);
      if( stunDefender )
        stunDefender = canStunType(defGun, atkModel);

      // Stun on death, for now?
      stunAttacker &= summary.defender.after.getHP() < 1;
      stunDefender &= summary.attacker.after.getHP() < 1;

      if (!stunAttacker && !stunDefender)
        return null;

      HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
      if( stunAttacker )
        victims.add(summary.attacker.unit);
      if( stunDefender )
        victims.add(summary.defender.unit);

      GameEvent stunEvent = new MassDamageEvent(null, victims, 0, false, true);
      GameEventQueue events = new GameEventQueue();
      events.add(stunEvent);
      return events;
    }
  } // ~CombatStunApplier

  private static boolean canStunType(KaijuWarsWeapon atkGun, KaijuWarsUnitModel defModel)
  {
    boolean canStun = false;
    if( atkGun.isAirWeapon && defModel.slowsAir )
      canStun = true;
    if( !atkGun.isAirWeapon && defModel.slowsLand )
      canStun = true;
    return canStun;
  }

} //~KaijuWarsWeapons
