package Units;

import Terrain.TerrainType;
import Units.KaijuWarsUnits.KaijuWarsUnitModel;

public class KaijuWarsWeapons
{
  // Multiply all movement (and ranges) by N
  public static final int KAIJU_SCALE_FACTOR = 1;
  // Percent damage that 1 ATK should do vs 1 kaijuCounter
  protected final static int KAIJU_DAMAGE_FACTOR = 80;
  protected final static int KAIJU_DAMAGE_BASE = 55;

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

    protected final static int SLOW_BONUS = 2;
    @Override
    public double getDamage(KaijuWarsUnitModel defender)
    {
      int counterPower = defender.kaijuCounter;
      if( !negateCounterBonuses )
      {
        if( isAirWeapon && defender.slowsAir )
          counterPower += SLOW_BONUS;
        if( !isAirWeapon && defender.slowsLand )
          counterPower += SLOW_BONUS;
        if( defender.resistsKaiju )
          counterPower += SLOW_BONUS;
      }

      int attack = 0;
      if( defender.isAirUnit() )
        attack = vsAir;
      else
        attack = vsLand;

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

    @Override
    public double getDamage(TerrainType target)
    {
      if( TerrainType.METEOR == target )
        return getDamageRatioStyle(vsLand, 4);
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
}
