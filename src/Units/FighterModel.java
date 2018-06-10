package Units;

import Engine.GameAction.ActionType;
import Units.Weapons.FighterMissiles;
import Units.Weapons.WeaponModel;

public class FighterModel extends AirModel
{

  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new FighterMissiles() };

  public FighterModel()
  {
    super("Fighter", UnitEnum.FIGHTER, ChassisEnum.AIR_HIGH, 20000, 99, 5, 9, actions, weapons);
  }
}
