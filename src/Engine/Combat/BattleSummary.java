package Engine.Combat;

import Terrain.Environment.Terrains;
import Units.Unit;

/**
 * This class simply provides information describing a battle, and is used more like a C-style struct than an object.
 */
public class BattleSummary
{
  public final Unit attacker;
  public final Unit defender;
  public final Terrains attackerTerrain;
  public final Terrains defenderTerrain;
  public double attackerHPLoss;
  public double defenderHPLoss;

  public BattleSummary( Unit atk, Unit def, Terrains atkTerrain, Terrains defTerrain, double atkHPLoss, double defHPLoss )
  {
    attacker = atk;
    defender = def;
    attackerTerrain = atkTerrain;
    defenderTerrain = defTerrain;
    attackerHPLoss = atkHPLoss;
    defenderHPLoss = defHPLoss;
  }
}
