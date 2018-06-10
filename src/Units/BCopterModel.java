package Units;

import Engine.GameAction.ActionType;
import Units.Weapons.CopterMGun;
import Units.Weapons.CopterRockets;
import Units.Weapons.WeaponModel;

public class BCopterModel extends AirModel
{

  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new CopterRockets(), new CopterMGun() };

  public BCopterModel()
  {
    super("B Copter", UnitEnum.B_COPTER, ChassisEnum.AIR_LOW, 9000, 99, 2, 6, actions, weapons);
  }
}
