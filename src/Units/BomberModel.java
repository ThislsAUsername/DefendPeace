package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.BomberBombs;
import Units.Weapons.WeaponModel;

public class BomberModel extends UnitModel
{

  private static final MoveType moveType = new Flight();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.AIRPORT };
  private static final WeaponModel[] weapons = { new BomberBombs() };

  public BomberModel()
  {
    super("Bomber", UnitEnum.BOMBER, 22000, 99, 5, 7, moveType, actions, healHabs, weapons);
  }
}
