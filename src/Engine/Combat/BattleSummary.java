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
  public final int attackerHPLoss, attackerHealthLoss;
  public final int defenderHPLoss, defenderHealthLoss;

  public BattleSummary(Unit atk, WeaponModel aw, Unit def, WeaponModel dw, TerrainType atkTerrain, TerrainType defTerrain,
      int atkHPLoss, int atkHealthLoss, int defHPLoss, int defHealthLoss)
  {
    attacker = atk;
    defender = def;
    attackerWeapon = aw;
    defenderWeapon = dw;
    attackerTerrain = atkTerrain;
    defenderTerrain = defTerrain;
    attackerHPLoss = atkHPLoss;
    attackerHealthLoss = atkHealthLoss;
    defenderHPLoss = defHPLoss;
    defenderHealthLoss = defHealthLoss;
  }
}
