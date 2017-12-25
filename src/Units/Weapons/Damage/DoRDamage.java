package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class DoRDamage extends DamageStrategy
{

  // format is [attacker][defender]
  // attacks: INFANTRYMGUN, MECHZOOKA, MECHMGUN, RECONMGUN, TANKCANNON, TANKMGUN, MD_TANKCANNON, MD_TANKMGUN, NEOCANNON, NEOMGUN, ARTILLERYCANNON, ROCKETS, ANTI_AIRMGUN, MOBILESAM, FIGHTERMISSILES, BOMBERBOMBS, B_COPTERROCKETS, B_COPTERMGUN, BATTLESHIPCANNON, CRUISERTORPEDOES, CRUISERMGUN, SUBTORPEDOES
  // defenders: INFANTRY, MECH, RECON, TANK, MD_TANK, NEOTANK, APC, ARTILLERY, ROCKETS, ANTI_AIR, MOBILESAM, FIGHTER, BOMBER, B_COPTER, T_COPTER, BATTLESHIP, CRUISER, LANDER, SUB
  private static int[][] damageChart = {
      { 55, 45, 12,  5,  5,  1, 14, 10, 20,  3, 20,  0,  0,  8, 30,  0,  0,  0,  0 },
      {  0,  0, 85, 55, 25, 15, 75, 70, 85, 55, 85,  0,  0,  0,  0,  0,  0,  0,  0 },
      { 65, 55, 18,  8,  5,  1, 20, 15, 35,  5, 35,  0,  0, 12, 35,  0,  0,  0,  0 },
      { 75, 65, 35,  8,  5,  1, 45, 45, 55,  8, 55,  0,  0, 18, 35,  0,  0,  0,  0 },
      {  0,  0, 85, 55, 35, 20, 75, 70, 85, 75, 85,  0,  0,  0,  0,  8,  9, 18,  9 },
      { 75, 70, 40,  8,  5,  1, 45, 45, 55,  8, 55,  0,  0, 18, 40,  0,  0,  0,  0 },
      {  0,  0, 95, 70, 55, 35, 90, 85, 90, 90, 90,  0,  0,  0,  0, 10, 12, 22, 12 },
      { 90, 80, 40,  8,  5,  1, 45, 45, 60,  8, 60,  0,  0, 24, 40,  0,  0,  0,  0 },
      {  0,  0,105, 85, 75, 55,105,105,105,105,105,  0,  0,  0,  0, 12, 14, 28, 14 },
      {105, 95, 45, 10, 10,  1, 45, 45, 65, 10, 65,  0,  0, 35, 45,  0,  0,  0,  0 },
      { 90, 85, 80, 60, 45, 35, 70, 75, 80, 65, 80,  0,  0,  0,  0, 45, 55, 65, 55 },
      { 95, 90, 90, 70, 55, 45, 80, 80, 85, 75, 85,  0,  0,  0,  0, 55, 65, 75, 65 },
      {105,105, 60, 15, 10,  5, 50, 50, 55, 45, 55, 70, 70,105,120,  0,  0,  0,  0 },
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,100,100,120,120,  0,  0,  0,  0 },
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 55, 65,120,120,  0,  0,  0,  0 },
      {115,110,105,105, 95, 75,105,105,105, 85, 95,  0,  0,  0,  0, 85, 50, 95, 95 },
      {  0,  0, 75, 70, 45, 35, 70, 65, 75, 10, 55,  0,  0,  0,  0, 25,  5, 25, 25 },
      { 75, 65, 30,  8,  8,  1, 20, 25, 35,  1, 25,  0,  0, 65, 95,  0,  0,  0,  0 },
      { 75, 70, 70, 65, 50, 40, 65, 70, 75, 65, 75,  0,  0,  0,  0, 45, 65, 75, 65 },
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 38, 28, 40, 95 },
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,105,105,120,120,  0,  0,  0,  0 },
      {  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 80, 20, 85, 55 }
      };

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(WeaponModel attack, UnitModel.UnitEnum defender)
  {
    return damageChart[attack.getIndex()][defender.ordinal()];
  }
}
