package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.AntiAirMGun;
import Units.Weapons.WeaponModel;

public class AntiAirModel extends UnitModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new AntiAirMGun() };

  public AntiAirModel()
  {
    super("Anti-Air", UnitEnum.ANTI_AIR, 8000, 60, 0, 6, moveType, actions, healHabs, weapons);
  }
}
