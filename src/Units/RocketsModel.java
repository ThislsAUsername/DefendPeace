package Units;

import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.Weapons.RocketRockets;
import Units.Weapons.WeaponModel;

public class RocketsModel extends LandModel
{

  private static final MoveType moveType = new Tires();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new RocketRockets() };

  public RocketsModel()
  {
    super("Rockets", UnitEnum.ROCKETS, ChassisEnum.TANK, 15000, 50, 0, 5, moveType, actions, weapons);
  }
}
