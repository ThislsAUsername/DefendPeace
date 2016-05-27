package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.FootStandard;
import Units.MoveTypes.MoveType;
import Units.Weapons.InfantryMGun;
import Units.Weapons.WeaponModel;

public class InfantryModel extends UnitModel
{

  private static final MoveType moveType = new FootStandard();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.CAPTURE, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new InfantryMGun() };

  public InfantryModel()
  {
    super("Infantry", Units.UnitModel.UnitEnum.INFANTRY, 1000, 99, 0, 3, moveType, actions, healHabs, weapons);
  }
}
