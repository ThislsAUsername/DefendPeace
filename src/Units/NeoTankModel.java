package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.NeoCannon;
import Units.Weapons.NeoMGun;
import Units.Weapons.WeaponModel;

public class NeotankModel extends UnitModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new NeoCannon(), new NeoMGun() };

  public NeotankModel()
  {
    super("Neotank", UnitEnum.NEOTANK, 22000, 99, 0, 6, moveType, actions, healHabs, weapons);
  }
}
