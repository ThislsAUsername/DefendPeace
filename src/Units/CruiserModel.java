package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.FloatHeavy;
import Units.MoveTypes.MoveType;
import Units.Weapons.CruiserMGun;
import Units.Weapons.CruiserTorpedoes;
import Units.Weapons.WeaponModel;

public class CruiserModel extends UnitModel
{

  private static final MoveType moveType = new FloatHeavy();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.SEAPORT };
  private static final WeaponModel[] weapons = { new CruiserTorpedoes(), new CruiserMGun() };

  public CruiserModel()
  {
    super("Cruiser", UnitEnum.CRUISER, ChassisEnum.SHIP, 18000, 99, 1, 6, moveType, actions, healHabs, weapons);
  }
}
