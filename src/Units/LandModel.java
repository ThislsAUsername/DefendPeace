package Units;


import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class LandModel extends UnitModel
{

  public LandModel(String pName, UnitEnum pType, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower,
      MoveType moveType, ActionType[] actions, WeaponModel[] weapons)
  {
    super(pName, pType, pChassis, cost, pFuelMax, pIdleFuelBurn, pMovePower, moveType, actions, true, false, false, weapons);
  }
}
