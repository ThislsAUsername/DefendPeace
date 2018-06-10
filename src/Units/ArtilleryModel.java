package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.ArtilleryCannon;
import Units.Weapons.WeaponModel;

public class ArtilleryModel extends LandModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new ArtilleryCannon() };

  public ArtilleryModel()
  {
    super("Artillery", UnitEnum.ARTILLERY, ChassisEnum.TANK, 6000, 50, 0, 5, moveType, actions, weapons);
  }
}
