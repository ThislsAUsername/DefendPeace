package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class DoRChassisDamage extends DamageStrategy
{

  // format is [attacker][defender]
  // defenders: TROOP, TANK, AIR_LOW, AIR_HIGH, SHIP, SUBMERGED
  
  // The highest damage vs a given chassis type, for each weapon.
  // Commented lines have the highest damages before the new DoR units, which aren't implemented currently.
  private static int[][] damageChart = {
      { 55, 20, 30,  0,  0,  0 }, // INFANTRYMGUN
      {  0, 85,  0,  0,  0,  0 }, // MECHZOOKA
      { 65, 35, 35,  0,  0,  0 }, // MECHMGUN
      { 75, 55, 35,  0,  0,  0 }, // RECONMGUN
//      { 25, 85,  0,  0, 18,  0 }, // TANKCANNON
      { 25, 85,  0,  0, 55,  0 }, // TANKCANNON
      { 75, 55, 40,  0,  0,  0 }, // TANKMGUN
//      { 30, 95,  0,  0, 22,  0 }, // MD_TANKCANNON
      { 30, 95,  0,  0, 55,  0 }, // MD_TANKCANNON
      { 90, 60, 40,  0,  0,  0 }, // MD_TANKMGUN
//      { 35,105,  0,  0, 28,  0 }, // NEOCANNON
      { 35,105,  0,  0, 65,  0 }, // NEOCANNON
      {105, 65, 45,  0,  0,  0 }, // NEOMGUN
//      { 90, 80,  0,  0, 65,  0 }, // ARTILLERYCANNON
      { 90, 80,  0,  0,100,  0 }, // ARTILLERYCANNON
//      { 95, 90,  0,  0, 75,  0 }, // ROCKETS
      { 95, 90,  0,  0,105,  0 }, // ROCKETS
//      {105, 60,120, 70,  0,  0 }, // ANTI_AIRMGUN
      {105, 60,120, 75,  0,  0 }, // ANTI_AIRMGUN
      {  0,  0,120,100,  0,  0 }, // MOBILESAM
//      {  0,  0,120, 65,  0,  0 }, // FIGHTERMISSILES
      {  0,  0,120, 80,  0,  0 }, // FIGHTERMISSILES
//      {115,105,  0,  0, 95,  0 }, // BOMBERBOMBS
      {115,105,  0,  0,120,  0 }, // BOMBERBOMBS
//      {  0, 75,  0,  0, 25,  0 }, // B_COPTERROCKETS
      {  0, 75,  0,  0, 85,  0 }, // B_COPTERROCKETS
      { 75, 35, 95,  0,  0,  0 }, // B_COPTERMGUN
//      { 75, 75,  0,  0, 75,  0 }, // BATTLESHIPCANNON
      { 75, 75,  0,  0, 95,  0 }, // BATTLESHIPCANNON
      {  0,  0,  0,  0, 95, 95 }, // CRUISERTORPEDOES
      {  0,  0,120,105,  0,  0 }, // CRUISERMGUN
//      {  0,  0,  0,  0, 85, 55}  // SUBTORPEDOES
      {  0,  0,  0,  0,120, 55 }  // SUBTORPEDOES
      };
  // Also, I left the cannon damages vs infantry because they amuse me.

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(WeaponModel attack, UnitModel defender)
  {
    return damageChart[attack.getIndex()][defender.chassis.ordinal()];
  }
}
