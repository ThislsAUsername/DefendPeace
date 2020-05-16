package Units;

import Units.AWBWUnits.AWBWUnitModel;

public class AWBWWeapons
{
  protected enum AWBWWeaponType
  {
    INFANTRYMGUN, MECHZOOKA, MECHMGUN,
    RECONMGUN, TANKCANNON, TANKMGUN, MD_TANKCANNON, MD_TANKMGUN, NEOCANNON, NEOMGUN, MEGACANNON, MEGAMGUN,
    ARTILLERYCANNON, ROCKETS, PIPEGUN, ANTI_AIRMGUN, MOBILESAM,
    FIGHTERMISSILES, BOMBERBOMBS, STEALTH_SHOTS, B_COPTERROCKETS, B_COPTERMGUN,
    CARRIERMISSILES, BATTLESHIPCANNON, CRUISERTORPEDOES, CRUISERMGUN, SUBTORPEDOES
  };
  
  protected final static boolean EATS_BULLETS = false;

  private static class AWBWWeapon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public AWBWWeaponType type;
    protected AWBWWeapon(AWBWWeaponType type, boolean infiniteAmmo, int minRange, int maxRange)
    {
      super(infiniteAmmo, minRange, maxRange);
      this.type = type;
    }
    protected AWBWWeapon(AWBWWeaponType type, boolean infiniteAmmo)
    {
      this(type, infiniteAmmo, 1, 1);
    }
    protected AWBWWeapon(AWBWWeaponType type)
    {
      this(type, true, 1, 1);
    }
    public AWBWWeapon(AWBWWeapon other)
    {
      this(other.type, other.hasInfiniteAmmo, other.minRange, other.maxRange);
      canFireAfterMoving = other.canFireAfterMoving;
    }
    @Override
    public WeaponModel clone()
    {
      return new AWBWWeapon(this);
    }

    @Override
    public int getDamage(AWBWUnitModel defender)
    {
      return damageChart[type.ordinal()][defender.type.ordinal()];
    }
  }

  public static class InfantryMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public InfantryMGun()
    {
      super(AWBWWeaponType.INFANTRYMGUN);
    }
  }

  public static class MechMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MechMGun()
    {
      super(AWBWWeaponType.MECHMGUN);
    }
  }

  public static class MechZooka extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MechZooka()
    {
      super(AWBWWeaponType.MECHZOOKA, EATS_BULLETS);
    }
  }

  public static class ReconMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public ReconMGun()
    {
      super(AWBWWeaponType.RECONMGUN);
    }
  }

  public static class TankMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public TankMGun()
    {
      super(AWBWWeaponType.TANKMGUN);
    }
  }

  public static class TankCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public TankCannon()
    {
      super(AWBWWeaponType.TANKCANNON, EATS_BULLETS);
    }
  }

  public static class MDTankMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MDTankMGun()
    {
      super(AWBWWeaponType.MD_TANKMGUN);
    }
  }

  public static class MDTankCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MDTankCannon()
    {
      super(AWBWWeaponType.MD_TANKCANNON, EATS_BULLETS);
    }
  }

  public static class NeoMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public NeoMGun()
    {
      super(AWBWWeaponType.NEOMGUN);
    }
  }

  public static class NeoCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public NeoCannon()
    {
      super(AWBWWeaponType.NEOCANNON, EATS_BULLETS);
    }
  }

  public static class MegaMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MegaMGun()
    {
      super(AWBWWeaponType.MEGAMGUN);
    }
  }

  public static class MegaCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MegaCannon()
    {
      super(AWBWWeaponType.MEGACANNON, EATS_BULLETS);
    }
  }

  public static class ArtilleryCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 3;

    public ArtilleryCannon()
    {
      super(AWBWWeaponType.ARTILLERYCANNON, EATS_BULLETS, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class RocketRockets extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public RocketRockets()
    {
      super(AWBWWeaponType.ROCKETS, EATS_BULLETS, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class PipeGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 5;

    public PipeGun()
    {
      super(AWBWWeaponType.PIPEGUN, EATS_BULLETS, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class AntiAirMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public AntiAirMGun()
    {
      super(AWBWWeaponType.ANTI_AIRMGUN, EATS_BULLETS);
    }
  }

  public static class MobileSAMWeapon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public MobileSAMWeapon()
    {
      super(AWBWWeaponType.MOBILESAM, EATS_BULLETS, MIN_RANGE, MAX_RANGE);
    }
  }

  // air

  public static class CopterMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public CopterMGun()
    {
      super(AWBWWeaponType.B_COPTERMGUN);
    }
  }

  public static class CopterRockets extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public CopterRockets()
    {
      super(AWBWWeaponType.B_COPTERROCKETS, EATS_BULLETS);
    }
  }

  public static class BomberBombs extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public BomberBombs()
    {
      super(AWBWWeaponType.BOMBERBOMBS, EATS_BULLETS);
    }
  }

  public static class FighterMissiles extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public FighterMissiles()
    {
      super(AWBWWeaponType.FIGHTERMISSILES, EATS_BULLETS);
    }
  }

  public static class StealthShots extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public StealthShots()
    {
      super(AWBWWeaponType.STEALTH_SHOTS, EATS_BULLETS);
    }
  }

  // sea

  public static class SubTorpedoes extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public SubTorpedoes()
    {
      super(AWBWWeaponType.SUBTORPEDOES, EATS_BULLETS);
    }
  }

  public static class BattleshipCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 6;

    public BattleshipCannon()
    {
      super(AWBWWeaponType.BATTLESHIPCANNON, EATS_BULLETS, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class CarrierMissiles extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 8;

    public CarrierMissiles()
    {
      super(AWBWWeaponType.CARRIERMISSILES, EATS_BULLETS, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class CruiserMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public CruiserMGun()
    {
      super(AWBWWeaponType.CRUISERMGUN);
    }
  }

  public static class CruiserTorpedoes extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public CruiserTorpedoes()
    {
      super(AWBWWeaponType.CRUISERTORPEDOES, EATS_BULLETS);
    }
  }


  // format is [attacker][defender]
  private static int[][] damageChart = {
// defenders:      INFANTRY, MECH, RECON, TANK, MD_TANK, NEOTANK, MEGATANK, APC, ARTILLERY, ROCKETS, PIPERUNNER, ANTI_AIR, MOBILESAM, FIGHTER, BOMBER, STEALTH, STEALTH_HIDE, B_COPTER, T_COPTER, BBOMB, CARRIER, BBOAT, BATTLESHIP, CRUISER, LANDER, SUB, SUB_SUB
/* INFANTRYMGUN     */{  55,   45,    12,    5,       1,       1,        1,  14,        15,      25,          5,        5,        25,       0,      0,       0,            0,        7,       30,     0,       0,     0,          0,       0,      0,   0,       0 }, /* INFANTRYMGUN     */
/* MECHZOOKA        */{   0,    0,    85,   55,      15,      15,        5,  75,        70,      85,         55,       65,        85,       0,      0,       0,            0,        0,        0,     0,       0,     0,          0,       0,      0,   0,       0 }, /* MECHZOOKA        */
/* MECHMGUN         */{  65,   55,    18,    6,       1,       1,        1,  20,        32,      35,          6,        6,        35,       0,      0,       0,            0,        9,       35,     0,       0,     0,          0,       0,      0,   0,       0 }, /* MECHMGUN         */
/* RECONMGUN        */{  70,   65,    35,    6,       1,       1,        1,  45,        45,      55,          6,        4,        28,       0,      0,       0,            0,       10,       35,     0,       0,     0,          0,       0,      0,   0,       0 }, /* RECONMGUN        */
/* TANKCANNON       */{  25,   25,    85,   55,      15,      15,       10,  75,        70,      85,         55,       65,        85,       0,      0,       0,            0,        0,        0,     0,       1,    10,          1,       5,     10,   1,       0 }, /* TANKCANNON       */
/* TANKMGUN         */{  75,   70,    40,    6,       1,       1,        1,  45,        45,      55,          6,        5,        30,       0,      0,       0,            0,       10,       40,     0,       0,     0,          0,       0,      0,   0,       0 }, /* TANKMGUN         */
/* MD_TANKCANNON    */{  30,   30,   105,   85,      55,      45,       25, 105,       105,     105,         85,      105,       105,       0,      0,       0,            0,        0,        0,     0,      10,    35,         10,      45,     35,  10,       0 }, /* MD_TANKCANNON    */
/* MD_TANKMGUN      */{ 105,   95,    45,    8,       1,       1,        1,  45,        45,      55,          8,        7,        35,       0,      0,       0,            0,       12,       45,     0,       0,     0,          0,       0,      0,   0,       0 }, /* MD_TANKMGUN      */
/* NEOCANNON        */{  35,   35,   125,  105,      75,      55,       35, 125,       115,     125,        105,      115,       125,       0,      0,       0,            0,        0,        0,     0,      15,    40,         15,      50,     40,  15,       0 }, /* NEOCANNON        */
/* NEOMGUN          */{ 125,  115,    65,   10,       1,       1,        1,  65,        65,      75,         10,       17,        55,       0,      0,       0,            0,       22,       55,     0,       0,     0,          0,       0,      0,   0,       0 }, /* NEOMGUN          */
/* MEGACANNON       */{  42,   42,   195,  180,     125,     115,       65, 195,       195,     195,        180,      195,       195,       0,      0,       0,            0,        0,        0,     0,      45,   105,         45,      65,     75,  45,       0 }, /* MEGACANNON       */
/* MEGAMGUN         */{ 135,  125,    65,   10,       1,       1,        1,  65,        65,      75,         10,       17,        55,       0,      0,       0,            0,       22,       55,     0,       0,     0,          0,       0,      0,   0,       0 }, /* MEGAMGUN         */
/* ARTILLERYCANNON  */{  90,   85,    80,   70,      45,      40,       15,  70,        75,      80,         70,       75,        80,       0,      0,       0,            0,        0,        0,     0,      45,    55,         40,      65,     55,  60,       0 }, /* ARTILLERYCANNON  */
/* ROCKETS          */{  95,   90,    90,   80,      55,      50,       25,  80,        80,      85,         80,       85,        90,       0,      0,       0,            0,        0,        0,     0,      60,    60,         55,      85,     60,  85,       0 }, /* ROCKETS          */
/* PIPEGUN          */{  95,   90,    90,   80,      55,      50,       25,  80,        80,      85,         80,       85,        90,      65,     75,      75,            0,      105,      105,   120,      60,    60,         55,      60,     60,  85,       0 }, /* PIPEGUN          */
/* ANTI_AIRMGUN     */{ 105,  105,    60,   25,      10,       5,        1,  50,        50,      55,         25,       45,        55,      65,     75,      75,            0,      120,      120,   120,       0,     0,          0,       0,      0,   0,       0 }, /* ANTI_AIRMGUN     */
/* MOBILESAM        */{   0,    0,     0,    0,       0,       0,        0,   0,         0,       0,          0,        0,         0,     100,    100,     100,            0,      120,      120,   120,       0,     0,          0,       0,      0,   0,       0 }, /* MOBILESAM        */
/* FIGHTERMISSILES  */{   0,    0,     0,    0,       0,       0,        0,   0,         0,       0,          0,        0,         0,      55,    100,      85,           85,      100,      100,   120,       0,     0,          0,       0,      0,   0,       0 }, /* FIGHTERMISSILES  */
/* BOMBERBOMBS      */{ 110,  110,   105,  105,      95,      90,       35, 105,       105,     105,        105,       95,       105,       0,      0,       0,            0,        0,        0,     0,     105,    75,         75,      85,     95,  95,       0 }, /* BOMBERBOMBS      */
/* STEALTH_SHOTS    */{  90,   90,    85,   75,      70,      60,       15,  85,        75,      85,         80,       50,        85,      45,     70,      55,           55,       85,       95,   120,      45,    65,         45,      35,     65,  55,       0 }, /* STEALTHPEWS      */
/* B_COPTERROCKETS  */{   0,    0,    55,   55,      25,      20,       10,  60,        65,      65,         55,       25,        65,       0,      0,       0,            0,        0,        0,     0,      25,    25,         25,      55,     25,  25,       0 }, /* B_COPTERROCKETS  */
/* B_COPTERMGUN     */{  75,   75,    30,    6,       1,       1,        0,  20,        25,      35,          6,        6,        35,       0,      0,       0,            0,       65,       95,     0,       0,     0,          0,       0,      0,   0,       0 }, /* B_COPTERMGUN     */
/* CARRIERMISSILES  */{   0,    0,     0,    0,       0,       0,        0,   0,         0,       0,          0,        0,         0,     100,    100,     100,            0,      115,      115,   120,       0,     0,          0,       0,      0,   0,       0 }, /* CARRIERMISSILES  */
/* BATTLESHIPCANNON */{  95,   90,    90,   80,      55,      50,       25,  80,        80,      85,         80,       85,        90,       0,      0,       0,            0,        0,        0,     0,      60,    95,         50,      95,     95,  95,       0 }, /* BATTLESHIPCANNON */
/* CRUISERTORPEDOES */{   0,    0,     0,    0,       0,       0,        0,   0,         0,       0,          0,        0,         0,       0,      0,       0,            0,        0,        0,     0,       5,    25,          0,       0,      0,  90,      90 }, /* CRUISERTORPEDOES */
/* CRUISERMGUN      */{   0,    0,     0,    0,       0,       0,        0,   0,         0,       0,          0,        0,         0,      55,     65,     100,            0,      115,      115,   120,       0,     0,          0,       0,      0,   0,       0 }, /* CRUISERMGUN      */
/* SUBTORPEDOES     */{   0,    0,     0,    0,       0,       0,        0,   0,         0,       0,          0,        0,         0,       0,      0,       0,            0,        0,        0,     0,      75,    95,         55,      25,     95,  55,      55 }  /* SUBTORPEDOES     */
      };

}
