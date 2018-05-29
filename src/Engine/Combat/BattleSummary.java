package Engine.Combat;

import Terrain.Environment.Terrains;
import Units.Unit;
import Units.Weapons.Weapon;

/**
 * This class simply provides information describing a battle, and is used more like a C-style struct than an object.
 */
public class BattleSummary
{
  public final Unit attacker;
  public final Unit defender;
  public final Weapon attackerWeapon;
  public final Weapon defenderWeapon;
  public final Terrains attackerTerrain;
  public final Terrains defenderTerrain;
  public double attackerHPLoss;
  public double defenderHPLoss;

  public BattleSummary(Unit atk, Weapon aw, Unit def, Weapon dw, Terrains atkTerrain, Terrains defTerrain, double atkHPLoss,
      double defHPLoss)
  {
    attacker = atk;
    defender = def;
    attackerWeapon = aw;
    defenderWeapon = dw;
    attackerTerrain = atkTerrain;
    defenderTerrain = defTerrain;
    attackerHPLoss = atkHPLoss;
    defenderHPLoss = defHPLoss;
  }
}
