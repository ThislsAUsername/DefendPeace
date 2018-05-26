package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class BHRDamage implements DamageStrategy
{

  // format is [attacker][defender]
  // defenders: INFANTRY, MECH, RECON, TANK, MD_TANK, NEOTANK, APC, ARTILLERY, ROCKETS, ANTI_AIR, MOBILESAM, FIGHTER, BOMBER, B_COPTER, T_COPTER, BATTLESHIP, CRUISER, LANDER, SUB
  private static int[][] damageChart = {
      { 55, 45, 12,  5,  1,  1, 14, 15, 25,  5, 25,  0,  0,  7, 30,  0,  0,  0,  0 }, // INFANTRYMGUN
      {  0,  0, 85, 55, 15, 15, 75, 70, 85, 65, 85,  0,  0,  0,  0,  0,  0,  0,  0 }, // MECHZOOKA
      { 65, 55, 18,  6,  1,  1, 20, 32, 35,  6, 35,  0,  0,  9, 35,  0,  0,  0,  0 }, // MECHMGUN
      { 70, 65, 35,  6,  1,  1, 45, 45, 55,  4, 28,  0,  0, 10, 35,  0,  0,  0,  0 }, // RECONMGUN
      { 25, 25, 85, 55, 15, 15, 75, 70, 85, 65, 85,  0,  0,  0,  0,  1,  5, 10,  1 }, // TANKCANNON
      { 75, 70, 40,  6,  1,  1, 45, 45, 55,  5, 30,  0,  0, 10, 40,  0,  0,  0,  0 }, // TANKMGUN
      { 30, 30,105, 85, 55, 45,105,105,105,105,105,  0,  0,  0,  0, 10, 45, 35, 10 }, // MD_TANKCANNON
      {105, 95, 45,  8,  1,  1, 45, 45, 55,  7, 35,  0,  0, 12, 45,  0,  0,  0,  0 }, // MD_TANKMGUN
      { 35, 35,125,105, 75, 55,125,115,125,115,125,  0,  0,  0,  0, 15, 50, 40, 15 }, // NEOCANNON
      {125,115, 65, 10,  1,  1, 65, 65, 75, 17, 55,  0,  0, 22, 55,  0,  0,  0,  0 }, // NEOMGUN
      { 90, 85, 80, 70, 45, 40, 70, 75, 80, 75, 80,  0,  0,  0,  0, 40, 65, 55, 60 }, // ARTILLERYCANNON
      { 95, 90, 90, 80, 55, 50, 80, 80, 85, 85, 90,  0,  0,  0,  0, 55, 85, 60, 85 }, // ROCKETS
      {105,105, 60, 25, 10,  5, 50, 50, 55, 45, 55, 65, 75,120,120,  0,  0,  0,  0 }, // ANTI_AIRMGUN
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,100,100,120,120,  0,  0,  0,  0 }, // MOBILESAM
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 55,100,100,100,  0,  0,  0,  0 }, // FIGHTERMISSILES
      {110,110,105,105, 95, 90,105,105,105, 95,105,  0,  0,  0,  0, 75, 85, 95, 95 }, // BOMBERBOMBS
      {  0,  0, 55, 55, 25, 20, 60, 65, 65, 25, 65,  0,  0,  0,  0, 25, 55, 25, 25 }, // B_COPTERROCKETS
      { 75, 75, 30,  6,  1,  1, 20, 25, 35,  6, 35,  0,  0, 65, 95,  0,  0,  0,  0 }, // B_COPTERMGUN
      { 95, 90, 90, 80, 55, 50, 80, 80, 85, 85, 90,  0,  0,  0,  0, 50, 95, 95, 95 }, // BATTLESHIPCANNON
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 90 }, // CRUISERTORPEDOES
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 55, 65,115,115,  0,  0,  0,  0 }, // CRUISERMGUN
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 55, 25, 95, 55 }  // SUBTORPEDOES
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
    return "AW2";
  }
}
