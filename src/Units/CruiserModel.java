package Units;

import Engine.GameAction.ActionType;
import Units.Weapons.CruiserMGun;
import Units.Weapons.CruiserTorpedoes;
import Units.Weapons.WeaponModel;

public class CruiserModel extends SeaModel
{

  private static final ActionType[] actions = { ActionType.ATTACK, ActionType.WAIT };
  private static final WeaponModel[] weapons = { new CruiserTorpedoes(), new CruiserMGun() };

  public CruiserModel()
  {
    super("Cruiser", UnitEnum.CRUISER, ChassisEnum.SHIP, 18000, 99, 1, 6, actions, weapons);
  }
}
