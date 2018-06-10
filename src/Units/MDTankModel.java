package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.MDTankCannon;
import Units.Weapons.MDTankMGun;
import Units.Weapons.WeaponModel;

public class MDTankModel extends LandModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new MDTankCannon(), new MDTankMGun() };

  public MDTankModel()
  {
    super("Medium Tank", UnitEnum.MD_TANK, ChassisEnum.TANK, 16000, 50, 0, 5, moveType, actions, weapons);
  }
}
