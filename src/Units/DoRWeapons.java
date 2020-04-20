package Units;

import Units.DoRUnits.DoRUnitModel;

public class DoRWeapons
{
  protected enum DoRWeaponType
  {
    INFANTRYMGUN, MECHZOOKA, MECHMGUN,
    RECONMGUN, FLAREMGUN, ANTI_AIRMGUN,
    TANKCANNON, TANKMGUN, MD_TANKCANNON, MD_TANKMGUN, WARCANNON, WARMGUN,
    ARTILLERYCANNON, ANTITANKCANNON, ROCKETS, MOBILESAM,
    FIGHTERMISSILES, BOMBERBOMBS, SEAPLANESHOTS,
    DUSTERMGUN, COPTERROCKETS, COPTERMGUN,
    GUNBOATGUN, CRUISERTORPEDOES, CRUISERMGUN, 
    SUBTORPEDOES, CARRIERMGUN, BATTLESHIPCANNON,
  };
  
  protected final static boolean USE_AMMO = true;

  private static class DoRWeapon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public DoRWeaponType type;
    protected DoRWeapon(DoRWeaponType type, boolean infiniteAmmo, int minRange, int maxRange)
    {
      super(infiniteAmmo, minRange, maxRange);
      this.type = type;
    }
    protected DoRWeapon(DoRWeaponType type, boolean infiniteAmmo)
    {
      this(type, infiniteAmmo, 1, 1);
    }
    protected DoRWeapon(DoRWeaponType type)
    {
      this(type, true, 1, 1);
    }
    public DoRWeapon(DoRWeapon other)
    {
      this(other.type, other.hasInfiniteAmmo, other.minRange, other.maxRange);
      canFireAfterMoving = other.canFireAfterMoving;
    }
    @Override
    public WeaponModel clone()
    {
      return new DoRWeapon(this);
    }

    @Override
    public double getDamage(DoRUnitModel defender)
    {
      return damageChart[type.ordinal()][defender.type.ordinal()];
    }
  }

  public static class InfantryMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public InfantryMGun()
    {
      super(DoRWeaponType.INFANTRYMGUN);
    }
  }

  public static class MechZooka extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public MechZooka()
    {
      super(DoRWeaponType.MECHZOOKA, USE_AMMO);
    }
  }

  public static class MechMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public MechMGun()
    {
      super(DoRWeaponType.MECHMGUN);
    }
  }

  public static class ReconMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public ReconMGun()
    {
      super(DoRWeaponType.RECONMGUN);
    }
  }

  public static class FlareMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public FlareMGun()
    {
      super(DoRWeaponType.FLAREMGUN);
    }
  }

  public static class AntiAirMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public AntiAirMGun()
    {
      super(DoRWeaponType.ANTI_AIRMGUN, USE_AMMO);
    }
  }

  public static class TankCannon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public TankCannon()
    {
      super(DoRWeaponType.TANKCANNON, USE_AMMO);
    }
  }

  public static class TankMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public TankMGun()
    {
      super(DoRWeaponType.TANKMGUN);
    }
  }

  public static class MDTankMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public MDTankMGun()
    {
      super(DoRWeaponType.MD_TANKMGUN);
    }
  }

  public static class MDTankCannon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public MDTankCannon()
    {
      super(DoRWeaponType.MD_TANKCANNON, USE_AMMO);
    }
  }

  public static class WarMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public WarMGun()
    {
      super(DoRWeaponType.WARMGUN);
    }
  }

  public static class WarCannon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public WarCannon()
    {
      super(DoRWeaponType.WARCANNON, USE_AMMO);
    }
  }

  public static class ArtilleryCannon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 3;

    public ArtilleryCannon()
    {
      super(DoRWeaponType.ARTILLERYCANNON, USE_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class AntiTankCannon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 1;
    private static final int MAX_RANGE = 3;

    public AntiTankCannon()
    {
      super(DoRWeaponType.ANTITANKCANNON, USE_AMMO, MIN_RANGE, MAX_RANGE);
      this.canFireAfterMoving = false;
    }
  }

  public static class RocketRockets extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public RocketRockets()
    {
      super(DoRWeaponType.ROCKETS, USE_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class MobileSAMWeapon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 6;

    public MobileSAMWeapon()
    {
      super(DoRWeaponType.MOBILESAM, USE_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  // air

  public static class FighterMissiles extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public FighterMissiles()
    {
      super(DoRWeaponType.FIGHTERMISSILES, USE_AMMO);
    }
  }

  public static class BomberBombs extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public BomberBombs()
    {
      super(DoRWeaponType.BOMBERBOMBS, USE_AMMO);
    }
  }

  public static class SeaplaneShots extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public SeaplaneShots()
    {
      super(DoRWeaponType.SEAPLANESHOTS, USE_AMMO);
    }
  }

  public static class DusterMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public DusterMGun()
    {
      super(DoRWeaponType.DUSTERMGUN, USE_AMMO);
    }
  }

  public static class CopterRockets extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public CopterRockets()
    {
      super(DoRWeaponType.COPTERROCKETS, USE_AMMO);
    }
  }

  public static class CopterMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public CopterMGun()
    {
      super(DoRWeaponType.COPTERMGUN);
    }
  }

  // sea

  public static class GunBoatGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public GunBoatGun()
    {
      super(DoRWeaponType.GUNBOATGUN, USE_AMMO);
    }
  }

  public static class CruiserTorpedoes extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public CruiserTorpedoes()
    {
      super(DoRWeaponType.CRUISERTORPEDOES, USE_AMMO);
    }
  }

  public static class CruiserMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public CruiserMGun()
    {
      super(DoRWeaponType.CRUISERMGUN);
    }
  }

  public static class SubTorpedoes extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public SubTorpedoes()
    {
      super(DoRWeaponType.SUBTORPEDOES, USE_AMMO);
    }
  }

  public static class BattleshipCannon extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public BattleshipCannon()
    {
      super(DoRWeaponType.BATTLESHIPCANNON, USE_AMMO, MIN_RANGE, MAX_RANGE);
      canFireAfterMoving = true;
    }
  }

  public static class CarrierMGun extends DoRWeapon
  {
    private static final long serialVersionUID = 1L;

    public CarrierMGun()
    {
      super(DoRWeaponType.CARRIERMGUN);
    }
  }


  // format is [attacker][defender]
  private static int[][] damageChart = {
// defenders:      INFANTRY, MECH,  BIKE, RECON, FLARE, ANTI_AIR, TANK, MD_TANK, WAR_TANK, ARTILLERY, ANTITANK, ROCKETS, MOBILESAM, RIG, FIGHTER, BOMBER, SEAPLANE, DUSTER, B_COPTER, T_COPTER, GUNBOAT, CRUISER, SUB, SUB_SUB, CARRIER, BATTLESHIP, LANDER
/* INFANTRYMGUN     */{  55,   45,    45,    12,    10,        3,    5,       5,        1,        10,       30,      20,        20,  14,       0,      0,        0,      0,        8,       30,       0,       0,   0,       0,       0,          0,      0 }, /* INFANTRYMGUN     */
/* MECHZOOKA        */{   0,    0,     0,    85,    80,       55,   55,      25,       15,        70,       55,      85,        85,  75,       0,      0,        0,      0,        0,        0,       0,       0,   0,       0,       0,          0,      0 }, /* MECHZOOKA        */
/* MECHMGUN         */{  65,   55,    55,    18,    15,        5,    8,       5,        1,        15,       35,      35,        35,  20,       0,      0,        0,      0,       12,       35,       0,       0,   0,       0,       0,          0,      0 }, /* MECHMGUN         */
/* RECONMGUN        */{  75,   65,    65,    35,    30,        8,    8,       5,        1,        45,       25,      55,        55,  45,       0,      0,        0,      0,       18,       35,       0,       0,   0,       0,       0,          0,      0 }, /* RECONMGUN        */
/* FLAREMGUN        */{  80,   70,    70,    60,    50,       45,   10,       5,        1,        45,       25,      55,        55,  45,       0,      0,        0,      0,       18,       35,       0,       0,   0,       0,       0,          0,      0 }, /* FLAREMGUN        */
/* ANTI_AIRMGUN     */{ 105,  105,   105,    60,    50,       45,   15,      10,        5,        50,       25,      55,        55,  50,      70,     70,       75,     75,      105,      120,       0,       0,   0,       0,       0,          0,      0 }, /* ANTI_AIRMGUN     */
/* TANKCANNON       */{   0,    0,     0,    85,    80,       75,   55,      35,       20,        70,       30,      85,        85,  75,       0,      0,        0,      0,        0,        0,      55,       9,   9,       0,       8,          8,     18 }, /* TANKCANNON       */
/* TANKMGUN         */{  75,   70,    70,    40,    35,        8,    8,       5,        1,        45,        1,      55,        55,  45,       0,      0,        0,      0,       18,       40,       0,       0,   0,       0,       0,          0,      0 }, /* TANKMGUN         */
/* MD_TANKCANNON    */{   0,    0,     0,    95,    90,       90,   70,      55,       35,        85,       35,      90,        90,  90,       0,      0,        0,      0,        0,        0,      55,      12,  12,       0,      10,         10,     22 }, /* MD_TANKCANNON    */
/* MD_TANKMGUN      */{  90,   80,    80,    40,    35,        8,    8,       5,        1,        45,        1,      60,        60,  45,       0,      0,        0,      0,       24,       40,       0,       0,   0,       0,       0,          0,      0 }, /* MD_TANKMGUN      */
/* WARCANNON        */{   0,    0,     0,   105,   105,      105,   85,      75,       55,       105,       40,     105,       105, 105,       0,      0,        0,      0,        0,        0,      65,      14,  14,       0,      12,         12,     28 }, /* WARCANNON        */
/* WARMGUN          */{ 105,   95,    95,    40,    40,       10,   10,      10,        1,        45,        1,      65,        65,  45,       0,      0,        0,      0,       35,       45,       0,       0,   0,       0,       0,          0,      0 }, /* WARMGUN          */
/* ARTILLERYCANNON  */{  90,   85,    85,    80,    75,       65,   60,      45,       35,        75,       55,      80,        80,  70,       0,      0,        0,      0,        0,        0,     100,      55,  55,       0,      45,         45,     65 }, /* ARTILLERYCANNON  */
/* ANTITANKCANNON   */{  75,   65,    65,    75,    75,       75,   75,      65,       55,        65,       55,      70,        70,  65,       0,      0,        0,      0,       45,       55,       0,       0,   0,       0,       0,          0,      0 }, /* ANTITANKCANNON   */
/* ROCKETS          */{  95,   90,    90,    90,    85,       75,   70,      55,       45,        80,       65,      85,        85,  80,       0,      0,        0,      0,        0,        0,     105,      65,  65,       0,      55,         55,     75 }, /* ROCKETS          */
/* MOBILESAM        */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,   0,     100,    100,      100,    100,      120,      120,       0,       0,   0,       0,       0,          0,      0 }, /* MOBILESAM        */
/* FIGHTERMISSILES  */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0, 100,      55,     65,       65,     80,      120,      120,       0,       0,   0,       0,       0,          0,      0 }, /* FIGHTERMISSILES  */
/* BOMBERBOMBS      */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,  55,       0,      0,        0,      0,        0,        0,     120,      50,  95,       0,      85,         85,     95 }, /* BOMBERBOMBS      */
/* SEAPLANESHOTS    */{ 110,  110,   105,   105,    95,       90,   35,     105,      105,       105,      105,      95,       105,   0,      45,     55,       55,     65,       85,       95,     105,      40,  55,       0,      65,         45,     85 }, /* SEAPLANESHOTS    */
/* DUSTERMGUN       */{  90,   90,    85,    75,    70,       60,   15,      85,       75,        85,       80,      50,        85,  45,      40,     45,       45,     55,       75,       90,       0,       0,   0,       0,       0,          0,      0 }, /* DUSTERMGUN       */
/* COPTERROCKETS    */{   0,    0,    55,    55,    25,       20,   10,      60,       65,        65,       55,      25,        65,   0,       0,      0,        0,      0,        0,        0,      85,       5,  25,       0,      25,         25,     25 }, /* COPTERROCKETS    */
/* COPTERMGUN       */{  75,   75,    30,     6,     1,        1,    0,      20,       25,        35,        6,       6,        35,   0,       0,      0,        0,      0,       65,       85,       0,       0,   0,       0,       0,          0,      0 }, /* COPTERMGUN       */
/* GUNBOATGUN       */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,   0,       0,      0,        0,      0,        0,        0,      75,      40,  40,       0,      40,         40,     55 }, /* GUNBOATGUN       */
/* CRUISERTORPEDOES */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,   0,       0,      0,        0,      0,        0,        0,      85,      28,  95,      95,      38,         38,     40 }, /* CRUISERTORPEDOES */
/* CRUISERMGUN      */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,   0,     105,    105,      105,    105,      120,      120,       0,       0,   0,       0,       0,          0,      0 }, /* CRUISERMGUN      */
/* SUBTORPEDOES     */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,  55,       0,      0,        0,      0,        0,        0,     120,      20,  55,      55,     110,         80,     85 }, /* SUBTORPEDOES     */
/* CARRIERMGUN      */{   0,    0,     0,     0,     0,        0,    0,       0,        0,         0,        0,       0,         0,  55,      35,     35,       40,     40,       45,       55,       0,       0,   0,       0,       0,          0,      0 }, /* CARRIERMGUN      */
/* BATTLESHIPCANNON */{  75,   70,    70,    70,    70,       65,   65,      50,       40,        70,       55,      75,        75,  65,       0,      0,        0,      0,        0,        0,      95,      65,  65,      50,       0,         45,     75 }  /* BATTLESHIPCANNON */
      };

}
