package Units;


import Engine.GameAction.ActionType;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class LandModel extends UnitModel
{

  public LandModel(String pName, UnitEnum pType, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower,
      MoveType moveType, ActionType[] actions, WeaponModel[] weapons)
  {
    // Land units are land units. THey use lots of different movetypes and I'm lazy
    super(pName, pType, pChassis, cost, pFuelMax, pIdleFuelBurn, pMovePower, moveType, actions, true, false, false, weapons);
  }
}
