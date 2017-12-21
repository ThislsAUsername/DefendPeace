package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.FighterMissiles;
import Units.Weapons.WeaponModel;

public class FighterModel extends UnitModel
{

  private static final MoveType moveType = new Flight();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.AIRPORT };
  private static final WeaponModel[] weapons = { new FighterMissiles() };

  public FighterModel()
  {
    super("Fighter", UnitEnum.FIGHTER, 20000, 99, 5, 9, moveType, actions, healHabs, weapons);
  }
}
