package Units.Weapons.Damage;

import Units.UnitModel;
import Units.Weapons.WeaponModel;

public class ChassisDamage extends DamageStrategy
{

  // format is [attacker][defender]
  // defenders: TROOP, TANK, SHIP, SUBMERGED, AIR_LOW, AIR_HIGH
  private static int[][] damageChart = {
      { 50,  5,  0,  0, 10,  0 }, // INFANTRYMGUN
      {  0, 35,  0,  0,  0,  0 }, // MECHZOOKA
      { 60,  8,  0,  0, 15,  0 }, // MECHMGUN
      { 70,  5,  0,  0, 20,  0 }, // RECONMGUN
      {  0, 45, 10,  0,  0,  0 }, // TANKCANNON
      { 70,  5,  0,  0, 20,  0 }, // TANKMGUN
      {  0, 60, 13,  0,  0,  0 }, // MD_TANKCANNON
      { 85,  5,  0,  0, 25,  0 }, // MD_TANKMGUN
      {  0, 80, 17,  0,  0,  0 }, // NEOCANNON
      {100, 10,  0,  0, 40,  0 }, // NEOMGUN
      { 90, 50, 55,  0,  0,  0 }, // ARTILLERYCANNON
      { 95, 60, 65,  0,  0,  0 }, // ROCKETS
      {105, 10,  0,  0,110, 70 }, // ANTI_AIRMGUN
      {  0,  0,  0,  0,120,100 }, // MOBILESAM
      {  0,  0,  0,  0,120, 60 }, // FIGHTERMISSILES
      {115,100, 80,  0,  0,  0 }, // BOMBERBOMBS
      {  0, 50, 25,  0,  0,  0 }, // B_COPTERROCKETS
      { 70,  8,  0,  0, 70,  0 }, // B_COPTERMGUN
      { 75, 65, 65,  0,  0,  0 }, // BATTLESHIPCANNON
      {  0,  0, 65, 65,  0,  0 }, // CRUISERTORPEDOES
      {  0,  0,  0,  0,120,105 }, // CRUISERMGUN
      {  0,  0, 60, 60,  0,  0 }  // SUBTORPEDOES
      };

  /**
   * @return returns its base damage against that unit type
   */
  public double getDamage(WeaponModel attack, UnitModel defender)
  {
    return damageChart[attack.getIndex()][defender.chassis.ordinal()];
  }
}
