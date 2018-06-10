package Units;


import Engine.GameAction.ActionType;
import Units.MoveTypes.Flight;
import Units.MoveTypes.MoveType;
import Units.Weapons.WeaponModel;

public class AirModel extends UnitModel
{

  private static final MoveType moveType = new Flight();

  public AirModel(String pName, UnitEnum pType, ChassisEnum pChassis, int cost, int pFuelMax, int pIdleFuelBurn, int pMovePower,
      ActionType[] actions, WeaponModel[] weapons)
  {
    super(pName, pType, pChassis, cost, pFuelMax, pIdleFuelBurn, pMovePower, moveType, actions, false, true, false, weapons);
  }
}
