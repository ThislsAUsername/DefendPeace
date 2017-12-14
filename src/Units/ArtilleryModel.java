package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tread;
import Units.Weapons.ArtilleryCannon;
import Units.Weapons.WeaponModel;

public class ArtilleryModel extends UnitModel
{

  private static final MoveType moveType = new Tread();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new ArtilleryCannon() };

  public ArtilleryModel()
  {
    super("Artillery", UnitEnum.ARTILLERY, 6000, 50, 0, 5, moveType, actions, healHabs, weapons);
  }
}
