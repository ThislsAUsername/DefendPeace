package Units;

import Engine.GameAction.ActionType;
import Units.Weapons.BattleshipCannon;
import Units.Weapons.WeaponModel;

public class BattleshipModel extends SeaModel
{

  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new BattleshipCannon() };

  public BattleshipModel()
  {
    super("Battleship", UnitEnum.BATTLESHIP, ChassisEnum.SHIP, 28000, 99, 1, 5, actions, weapons);
  }
}
