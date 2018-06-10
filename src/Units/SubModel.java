package Units;

import Engine.GameAction.ActionType;
import Units.Weapons.SubTorpedoes;
import Units.Weapons.WeaponModel;

public class SubModel extends SeaModel
{

  // TODO: add submerge
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new SubTorpedoes() };

  public SubModel()
  {
    super("Submarine", UnitEnum.SUB, ChassisEnum.SHIP, 20000, 60, 1, 6, actions, weapons);
  }
}
