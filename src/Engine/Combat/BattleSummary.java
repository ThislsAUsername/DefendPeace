package Engine.Combat;

import Terrain.TerrainType;
import Units.Unit;
import Units.WeaponModel;

/**
 * This class simply provides information describing a battle, and is used more like a C-style struct than an object.
 */
public class BattleSummary
{
  public final Unit attacker;
  public final Unit defender;
  public final WeaponModel attackerWeapon;
  public final WeaponModel defenderWeapon;
  public final TerrainType attackerTerrain;
  public final TerrainType defenderTerrain;
  public final double attackerHPLoss, attackerHealthLoss;
  public final double defenderHPLoss, defenderHealthLoss;

  public BattleSummary(Unit atk, WeaponModel aw, Unit def, WeaponModel dw, TerrainType atkTerrain, TerrainType defTerrain,
      double atkHPLoss, double atkHealthLoss,
      double defHPLoss, double defHealthLoss)
  {
    attacker = atk;
    defender = def;
    attackerWeapon = aw;
    defenderWeapon = dw;
    attackerTerrain = atkTerrain;
    defenderTerrain = defTerrain;
    attackerHPLoss = atkHPLoss;
    defenderHPLoss = defHPLoss;
    attackerHealthLoss = atkHealthLoss;
    defenderHealthLoss = defHealthLoss;
  }
}
