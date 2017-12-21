package Units;

import Engine.GameAction.ActionType;
import Terrain.Environment.Terrains;
import Units.MoveTypes.MoveType;
import Units.MoveTypes.Tires;
import Units.Weapons.MobileSAMWeapon;
import Units.Weapons.WeaponModel;

public class MobileSAMModel extends UnitModel
{

  private static final MoveType moveType = new Tires();
  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final Terrains[] healHabs = { Terrains.CITY, Terrains.FACTORY, Terrains.HQ };
  private static final WeaponModel[] weapons = { new MobileSAMWeapon() };

  public MobileSAMModel()
  {
    super("Mobile SAM", UnitEnum.MOBILESAM, 12000, 50, 0, 4, moveType, actions, healHabs, weapons);
  }
}
