package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class BHRDamage implements DamageStrategy
{

  // format is [attacker][defender]
  private static int[][] damageChart = {
// defenders:      INFANTRY, MECH, RECON, TANK, MD_TANK, NEOTANK, APC, ARTILLERY, ROCKETS, ANTI_AIR, MOBILESAM, FIGHTER, BOMBER, B_COPTER, T_COPTER, BATTLESHIP, CRUISER, LANDER, SUB, SUB_SUB
/* INFANTRYMGUN     */{  55,   45,    12,    5,       1,       1,  14,        15,      25,        5,        25,       0,      0,        7,       30,          0,       0,      0,   0,       0 }, // INFANTRYMGUN
/* MECHZOOKA        */{   0,    0,    85,   55,      15,      15,  75,        70,      85,       65,        85,       0,      0,        0,        0,          0,       0,      0,   0,       0 }, // MECHZOOKA
/* MECHMGUN         */{  65,   55,    18,    6,       1,       1,  20,        32,      35,        6,        35,       0,      0,        9,       35,          0,       0,      0,   0,       0 }, // MECHMGUN
/* RECONMGUN        */{  70,   65,    35,    6,       1,       1,  45,        45,      55,        4,        28,       0,      0,       10,       35,          0,       0,      0,   0,       0 }, // RECONMGUN
/* TANKCANNON       */{  25,   25,    85,   55,      15,      15,  75,        70,      85,       65,        85,       0,      0,        0,        0,          1,       5,     10,   1,       0 }, // TANKCANNON
/* TANKMGUN         */{  75,   70,    40,    6,       1,       1,  45,        45,      55,        5,        30,       0,      0,       10,       40,          0,       0,      0,   0,       0 }, // TANKMGUN
/* MD_TANKCANNON    */{  30,   30,   105,   85,      55,      45, 105,       105,     105,      105,       105,       0,      0,        0,        0,         10,      45,     35,  10,       0 }, // MD_TANKCANNON
/* MD_TANKMGUN      */{ 105,   95,    45,    8,       1,       1,  45,        45,      55,        7,        35,       0,      0,       12,       45,          0,       0,      0,   0,       0 }, // MD_TANKMGUN
/* NEOCANNON        */{  35,   35,   125,  105,      75,      55, 125,       115,     125,      115,       125,       0,      0,        0,        0,         15,      50,     40,  15,       0 }, // NEOCANNON
/* NEOMGUN          */{ 125,  115,    65,   10,       1,       1,  65,        65,      75,       17,        55,       0,      0,       22,       55,          0,       0,      0,   0,       0 }, // NEOMGUN
/* ARTILLERYCANNON  */{  90,   85,    80,   70,      45,      40,  70,        75,      80,       75,        80,       0,      0,        0,        0,         40,      65,     55,  60,       0 }, // ARTILLERYCANNON
/* ROCKETS          */{  95,   90,    90,   80,      55,      50,  80,        80,      85,       85,        90,       0,      0,        0,        0,         55,      85,     60,  85,       0 }, // ROCKETS
/* ANTI_AIRMGUN     */{ 105,  105,    60,   25,      10,       5,  50,        50,      55,       45,        55,      65,     75,      120,      120,          0,       0,      0,   0,       0 }, // ANTI_AIRMGUN
/* MOBILESAM        */{   0,    0,     0,    0,       0,       0,   0,         0,       0,        0,         0,     100,    100,      120,      120,          0,       0,      0,   0,       0 }, // MOBILESAM
/* FIGHTERMISSILES  */{   0,    0,     0,    0,       0,       0,   0,         0,       0,        0,         0,      55,    100,      100,      100,          0,       0,      0,   0,       0 }, // FIGHTERMISSILES
/* BOMBERBOMBS      */{ 110,  110,   105,  105,      95,      90, 105,       105,     105,       95,       105,       0,      0,        0,        0,         75,      85,     95,  95,       0 }, // BOMBERBOMBS
/* B_COPTERROCKETS  */{   0,    0,    55,   55,      25,      20,  60,        65,      65,       25,        65,       0,      0,        0,        0,         25,      55,     25,  25,       0 }, // B_COPTERROCKETS
/* B_COPTERMGUN     */{  75,   75,    30,    6,       1,       1,  20,        25,      35,        6,        35,       0,      0,       65,       95,          0,       0,      0,   0,       0 }, // B_COPTERMGUN
/* BATTLESHIPCANNON */{  95,   90,    90,   80,      55,      50,  80,        80,      85,       85,        90,       0,      0,        0,        0,         50,      95,     95,  95,       0 }, // BATTLESHIPCANNON
/* CRUISERTORPEDOES */{   0,    0,     0,    0,       0,       0,   0,         0,       0,        0,         0,       0,      0,        0,        0,          0,       0,      0,  90,      90 }, // CRUISERTORPEDOES
/* CRUISERMGUN      */{   0,    0,     0,    0,       0,       0,   0,         0,       0,        0,         0,      55,     65,      115,      115,          0,       0,      0,   0,       0 }, // CRUISERMGUN
/* SUBTORPEDOES     */{   0,    0,     0,    0,       0,       0,   0,         0,       0,        0,         0,       0,      0,        0,        0,         55,      25,     95,  55,      55 }  // SUBTORPEDOES
      };

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(WeaponModel attack, UnitModel defender)
  {
    return damageChart[attack.getIndex()][defender.type.ordinal()];
  }

  public String getDescription()
  {
    return "Black Hole Rising";
  }
}
