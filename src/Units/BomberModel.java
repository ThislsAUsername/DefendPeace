package Units;

import Engine.GameAction.ActionType;
import Units.Weapons.BomberBombs;
import Units.Weapons.WeaponModel;

public class BomberModel extends AirModel
{

  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new BomberBombs() };

  public BomberModel()
  {
    super("Bomber", UnitEnum.BOMBER, ChassisEnum.AIR_HIGH, 22000, 99, 5, 7, actions, weapons);
  }
}
