package Units;

import Units.AWBWUnits.AWBWUnitModel;

public class AWBWWeapons
{
  protected enum WeaponType
  {
    INFANTRYMGUN, MECHZOOKA, MECHMGUN,
    RECONMGUN, TANKCANNON, TANKMGUN, MD_TANKCANNON, MD_TANKMGUN, NEOCANNON, NEOMGUN, MEGACANNON, MEGAMGUN,
    ARTILLERYCANNON, ROCKETS, PIPEGUN, ANTI_AIRMGUN, MOBILESAM,
    FIGHTERMISSILES, BOMBERBOMBS, STEALTH_SHOTS, B_COPTERROCKETS, B_COPTERMGUN,
    CARRIERMISSILES, BATTLESHIPCANNON, CRUISERTORPEDOES, CRUISERMGUN, SUBTORPEDOES
  };

  private static class AWBWWeapon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public WeaponType type;
    protected AWBWWeapon(WeaponType type, int ammo, int minRange, int maxRange)
    {
      super(ammo, minRange, maxRange);
      this.type = type;
    }
    protected AWBWWeapon(WeaponType type, int ammo)
    {
      this(type, ammo, 1, 1);
    }
    protected AWBWWeapon(WeaponType type)
    {
      this(type, -1, 1, 1);
    }
    public AWBWWeapon(AWBWWeapon other)
    {
      this(other.type, other.maxAmmo, other.minRange, other.maxRange);
    }
    @Override
    public WeaponModel clone()
    {
      return new AWBWWeapon(this);
    }

    @Override
    public double getDamage(AWBWUnitModel defender)
    {
      return damageChart[type.ordinal()][defender.type.ordinal()];
    }
  }

  public static class InfantryMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public InfantryMGun()
    {
      super(WeaponType.INFANTRYMGUN);
    }
  }

  public static class MechMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MechMGun()
    {
      super(WeaponType.MECHMGUN);
    }
  }

  public static class MechZooka extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 3;

    public MechZooka()
    {
      super(WeaponType.MECHZOOKA, MAX_AMMO);
    }
  }

  public static class ReconMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public ReconMGun()
    {
      super(WeaponType.RECONMGUN);
    }
  }

  public static class TankMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public TankMGun()
    {
      super(WeaponType.TANKMGUN);
    }
  }

  public static class TankCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public TankCannon()
    {
      super(WeaponType.TANKCANNON, MAX_AMMO);
    }
  }

  public static class MDTankMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MDTankMGun()
    {
      super(WeaponType.MD_TANKMGUN);
    }
  }

  public static class MDTankCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 8;

    public MDTankCannon()
    {
      super(WeaponType.MD_TANKCANNON, MAX_AMMO);
    }
  }

  public static class NeoMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public NeoMGun()
    {
      super(WeaponType.NEOMGUN);
    }
  }

  public static class NeoCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public NeoCannon()
    {
      super(WeaponType.NEOCANNON, MAX_AMMO);
    }
  }

  public static class MegaMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public MegaMGun()
    {
      super(WeaponType.MEGAMGUN);
    }
  }

  public static class MegaCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 3;

    public MegaCannon()
    {
      super(WeaponType.MEGACANNON, MAX_AMMO);
    }
  }

  public static class ArtilleryCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 3;

    public ArtilleryCannon()
    {
      super(WeaponType.ARTILLERYCANNON, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class RocketRockets extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public RocketRockets()
    {
      super(WeaponType.ROCKETS, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class PipeGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 5;

    public PipeGun()
    {
      super(WeaponType.PIPEGUN, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class AntiAirMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public AntiAirMGun()
    {
      super(WeaponType.ANTI_AIRMGUN, MAX_AMMO);
    }
  }

  public static class MobileSAMWeapon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public MobileSAMWeapon()
    {
      super(WeaponType.MOBILESAM, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  // air

  public static class CopterMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public CopterMGun()
    {
      super(WeaponType.B_COPTERMGUN);
    }
  }

  public static class CopterRockets extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 6;

    public CopterRockets()
    {
      super(WeaponType.B_COPTERROCKETS, MAX_AMMO);
    }
  }

  public static class BomberBombs extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public BomberBombs()
    {
      super(WeaponType.BOMBERBOMBS, MAX_AMMO);
    }
  }

  public static class FighterMissiles extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public FighterMissiles()
    {
      super(WeaponType.FIGHTERMISSILES, MAX_AMMO);
    }
  }

  public static class StealthShots extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 6;

    public StealthShots()
    {
      super(WeaponType.STEALTH_SHOTS, MAX_AMMO);
    }
  }

  // sea

  public static class SubTorpedoes extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 6;

    public SubTorpedoes()
    {
      super(WeaponType.SUBTORPEDOES, MAX_AMMO);
    }
  }

  public static class BattleshipCannon extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 6;

    public BattleshipCannon()
    {
      super(WeaponType.BATTLESHIPCANNON, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class CarrierMissiles extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 8;

    public CarrierMissiles()
    {
      super(WeaponType.CARRIERMISSILES, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class CruiserMGun extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;

    public CruiserMGun()
    {
      super(WeaponType.CRUISERMGUN);
    }
  }

  public static class CruiserTorpedoes extends AWBWWeapon
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public CruiserTorpedoes()
    {
      super(WeaponType.CRUISERTORPEDOES, MAX_AMMO);
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
