package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class ChassisDamage implements DamageStrategy
{

  // format is [attacker][defender]
  // defenders: TROOP, TANK, AIR_LOW, AIR_HIGH, SHIP, SUBMERGED
  private static int[][] damageChart = {
      { 55, 25, 30,  0,  0,  0 }, // INFANTRYMGUN
      {  0, 85,  0,  0,  0,  0 }, // MECHZOOKA
      { 65, 35, 35,  0,  0,  0 }, // MECHMGUN
      { 70, 55, 55,  0,  0,  0 }, // RECONMGUN
      { 25, 85,  0,  0, 10,  0 }, // TANKCANNON
      { 75, 55, 40,  0,  0,  0 }, // TANKMGUN
      { 30,105,  0,  0, 45,  0 }, // MD_TANKCANNON
      {105, 55, 45,  0,  0,  0 }, // MD_TANKMGUN
      { 35,125,  0,  0, 50,  0 }, // NEOCANNON
      {125, 75, 55,  0,  0,  0 }, // NEOMGUN
      { 90, 80,  0,  0, 65,  0 }, // ARTILLERYCANNON
      { 95, 90,  0,  0, 85,  0 }, // ROCKETS
      {105, 60,120, 75,  0,  0 }, // ANTI_AIRMGUN
      {  0,  0,120,100,  0,  0 }, // MOBILESAM
      {  0,  0,100,100,  0,  0 }, // FIGHTERMISSILES
      {110,105,  0,  0, 95,  0 }, // BOMBERBOMBS
      {  0, 65,  0,  0, 55,  0 }, // B_COPTERROCKETS
      { 75, 35, 95,  0,  0,  0 }, // B_COPTERMGUN
      { 95, 90,  0,  0, 95,  0 }, // BATTLESHIPCANNON
      {  0,  0,  0,  0, 90, 90 }, // CRUISERTORPEDOES
      {  0,  0,115, 65,  0,  0 }, // CRUISERMGUN
      {  0,  0,  0,  0, 95, 55 }  // SUBTORPEDOES
      };

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(WeaponModel attack, UnitModel defender)
  {
    return damageChart[attack.getIndex()][defender.chassis.ordinal()];
  }

  public String getDescription()
  {
    return "Chassis Only";
  }
}
