package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.TankCannon;
import Units.Weapons.TankMGun;
import Units.Weapons.WeaponModel;

public class TankModel extends LandModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new TankCannon(), new TankMGun() };

  public TankModel()
  {
    super("Tank", UnitEnum.TANK, ChassisEnum.TANK, 7000, 70, 0, 6, moveType, actions, weapons);
  }
}
