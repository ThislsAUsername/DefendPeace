package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.SubTorpedoes;
import Units.Weapons.WeaponModel;

public class SubModel extends UnitModel
{

  private static final MoveType moveType = new FloatHeavy();
  // TODO: add submerge
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.SEAPORT };
  private static final WeaponModel[] weapons = { new SubTorpedoes() };

  public SubModel()
  {
    super("Submarine", UnitEnum.SUB, ChassisEnum.SHIP, 20000, 60, 1, 6, moveType, actions, healHabs, weapons);
  }
}
