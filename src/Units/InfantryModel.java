package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.Weapons.InfantryMGun;
import Units.Weapons.WeaponModel;

public class InfantryModel extends LandModel
{

  private static final MoveType moveType = new FootStandard();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.CAPTURE, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new InfantryMGun() };

  public InfantryModel()
  {
    super("Infantry", UnitEnum.INFANTRY, ChassisEnum.TROOP, 1000, 99, 0, 3, moveType, actions, weapons);
  }
}
