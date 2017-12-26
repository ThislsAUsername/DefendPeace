package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.Weapons.RocketRockets;
import Units.Weapons.WeaponModel;

public class RocketsModel extends UnitModel
{

  private static final MoveType moveType = new Tires();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new RocketRockets() };

  public RocketsModel()
  {
    super("Rockets", UnitEnum.ROCKETS, ChassisEnum.TRUCK, 15000, 50, 0, 5, moveType, actions, healHabs, weapons);
  }
}
