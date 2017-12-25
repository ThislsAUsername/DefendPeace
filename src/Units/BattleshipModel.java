package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.BattleshipCannon;
import Units.Weapons.WeaponModel;

public class BattleshipModel extends UnitModel
{

  private static final MoveType moveType = new FloatHeavy();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.SEAPORT };
  private static final WeaponModel[] weapons = { new BattleshipCannon() };

  public BattleshipModel()
  {
    super("Battleship", UnitEnum.BATTLESHIP, ChassisEnum.SHIP, 28000, 99, 1, 5, moveType, actions, healHabs, weapons);
  }
}
