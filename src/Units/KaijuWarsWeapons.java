package Units;

import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.StateTrackers.StateTracker;
import Engine.StateTrackers.UnitTurnPositionTracker;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.GameMap;
import Terrain.TerrainType;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;

public class KaijuWarsWeapons
{
  // Multiply all movement (and ranges) by N
  public static final int KAIJU_SCALE_FACTOR  = 1;
  // Percent damage that 1 ATK should do vs 1 kaijuCounter
  public final static int KAIJU_DAMAGE_FACTOR = 80;
  public final static int KAIJU_DAMAGE_BASE   = 55;

  public final static int SLOW_BONUS          = 2;
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
      if( maxRange > 1 )
        negateCounterBonuses = true;
    }
    protected KaijuWarsWeapon(int vsLand, int vsAir)
    {
      this(vsLand, vsAir, 1, 1);
    }
    public KaijuWarsWeapon(KaijuWarsWeapon other)
    {
      this(other.vsLand, other.vsAir, other.rangeMin, other.rangeMax);
      canFireAfterMoving = other.canFireAfterMoving;
    }
    @Override
    public WeaponModel clone()
    {
      return new KaijuWarsWeapon(this);
    }

    @Override
    public double getDamage(KaijuWarsUnitModel defender)
    {
      int counterPower = deriveCounter(this, defender);

      int attack = deriveAttack(this, defender);

      return KaijuWarsWeapons.getDamage(attack, counterPower);
    }

    @Override
    public double getDamage(TerrainType target)
    {
      if( TerrainType.METEOR == target )
        return KaijuWarsWeapons.getDamage(vsLand, TERRAIN_DURABILITY);
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
    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 2*KAIJU_SCALE_FACTOR;

    public Missile()
    {
      super(2, 3, MIN_RANGE, MAX_RANGE);
      canFireAfterMoving = true;
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
    private static final int MAX_RANGE = 3*KAIJU_SCALE_FACTOR;

    public Artillery()
    {
      super(1, 0, MIN_RANGE, MAX_RANGE);
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
  // TODO: slow logic?
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
    private static final int MAX_RANGE = 5*KAIJU_SCALE_FACTOR;

    public CannonOfBoom()
    {
      super(3, 2, MIN_RANGE, MAX_RANGE);
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
  public static int deriveCounter(KaijuWarsWeapon gun, KaijuWarsUnitModel defender)
  {
    int counterPower = defender.kaijuCounter;
    if( !gun.negateCounterBonuses )
    {
      if( gun.isAirWeapon && defender.slowsAir )
        counterPower += SLOW_BONUS;
      if( !gun.isAirWeapon && defender.slowsLand )
        counterPower += SLOW_BONUS;
      if( defender.resistsKaiju )
        counterPower += SLOW_BONUS;
    }
    return counterPower;
  }

  public static double getDamage(int attack, int counterPower)
  {
    return getDamageRatioStyle(attack, counterPower);
  }

  /**
   * Produces damage numbers based on attack/kaijuCounter
   */
  public static double getDamageRatioStyle(int attack, int counterPower)
  {
    // 1-based instead of 0-based
    int durability = 1 + counterPower;
    int damage = attack * 1000 / durability;
    // Round properly
    damage += 5;
    damage /= 10;

    return damage * KAIJU_DAMAGE_FACTOR / 100;
  }

  /**
   * Produces damage numbers centered around KAIJU_DAMAGE_BASE
   */
  public static double getDamageShiftingStyle(int attack, int durability)
  {
    int damage = KAIJU_DAMAGE_BASE;

    int finalPower = attack - durability;
    if( finalPower > -3 )
      damage += finalPower * 10;
    else if( finalPower > -8 )
      damage = damage - 10 + finalPower * 5;
    else
      damage = 2 * (12 + finalPower);

    return damage;
  }

  // Implements all +ATK and +counter as base damage changes
  public static class KaijuWarsFightMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    // Rewrite base damage in both of these functions so we can beat up both terrain and units with situational boosts
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      // Assume only one weapon, since that holds for this unit set
      KaijuWarsWeapon gun = (KaijuWarsWeapon) params.attacker.model.weapons.get(0);
      int attack = gun.vsLand;

      // TODO: The target coord is unavailable. Derp.
      //final TerrainType atkEnv = params.attacker.env.terrainType;
      //int attackBoost = getAttackBoost(params.attacker.unit, params.map, params.attacker.coord, atkEnv, params.defender.coord);
      //attack += attackBoost;

      params.baseDamage = getDamage(attack, TERRAIN_DURABILITY);
    }

    @Override
    public void modifyUnitAttackOnUnit(BattleParams params)
    {
      // Assume only one weapon, since that holds for this unit set
      KaijuWarsWeapon gun = (KaijuWarsWeapon) params.attacker.model.weapons.get(0);
      KaijuWarsUnitModel defModel = (KaijuWarsUnitModel) params.defender.model;
      final TerrainType atkEnv = params.attacker.env.terrainType;
      final TerrainType defEnv = params.defender.env.terrainType;

      int counterBoost = getCounterBoost(params.defender.unit, params.map, defEnv);
      int counterPower = deriveCounter(gun, defModel);
      counterPower += counterBoost;

      int attackBoost = getAttackBoost(params.attacker.unit, params.map, params.attacker.coord, atkEnv, params.defender.coord);
      int attack = deriveAttack(gun, defModel);
      attack += attackBoost;

      params.baseDamage = getDamage(attack, counterPower);
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
  public static int getCounterBoost(Unit defender, GameMap map, TerrainType defEnv)
  {
    int counterBoost = 0;
    KaijuWarsUnitModel defModel = (KaijuWarsUnitModel) defender.model;
    if( defModel.entrenches )
    {
      // This is a bit smelly, but better than the alternative?
      UnitTurnPositionTracker tracker = StateTracker.instance(map.game, UnitTurnPositionTracker.class);
      if( tracker.stoodStill(defender) )
        counterBoost += SLOW_BONUS;
    }
    if( defModel.divineWind &&
        (defEnv == TerrainType.GRASS || defEnv == TerrainType.SEA) )
      counterBoost += SLOW_BONUS;
    return counterBoost;
  }
  /**
   * Gets any situational attack power boost from unit mechanics or assumed-enabled tactics/techs
   */
  public static int getAttackBoost(Unit attacker, GameMap map, XYCoord atkCoord, TerrainType atkEnv, XYCoord targetCoord)
  {
    int attackBoost = 0;
    // Rangefinders boost - these units are pretty stally, so why not?
    if( attacker.model.isLandUnit() &&
        atkEnv == TerrainType.MOUNTAIN )
      attackBoost += SLOW_BONUS;
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

} //~KaijuWarsWeapons
