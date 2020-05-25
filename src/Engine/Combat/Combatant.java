package Engine.Combat;

import Units.Unit;
import Units.WeaponModel;

/**
 * Represents the state data necessary to track attacker or defender in a BattleParams
 */
public class Combatant
{
  public final Unit body;
  public final WeaponModel gun;
  public final int x, y;

  public Combatant(Unit belligerent, WeaponModel weapon, int x, int y)
  {
    this.body = belligerent;
    this.gun = weapon;
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString()
  {
    return "CO " + body.CO + " attacking with " + body;
  }
}
