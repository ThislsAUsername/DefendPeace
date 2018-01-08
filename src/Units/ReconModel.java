package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.Weapons.ReconMGun;
import Units.Weapons.WeaponModel;

public class ReconModel extends UnitModel
{

  private static final MoveType moveType = new Tires();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new ReconMGun() };

  public ReconModel()
  {
    super("Recon", Units.UnitModel.UnitEnum.RECON, ChassisEnum.TANK, 4000, 80, 0, 8, moveType, actions, healHabs, weapons);
  }
}
