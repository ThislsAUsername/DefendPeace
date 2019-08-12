package Units;

import Engine.UnitActionType;

public class StealthHideModel extends StealthModel
{
  private static final long serialVersionUID = 1L;
  private static final int IDLE_FUEL_BURN = 8;

  public StealthHideModel()
  {
    super();
    type = UnitEnum.STEALTH_HIDE;
    chassis = ChassisEnum.AIR_HIGH;
    idleFuelBurn = IDLE_FUEL_BURN;
    hidden = true;
  }
  
  @Override
  protected void addStealthAction()
  {
    possibleActions.add(new UnitActionType.Transform(UnitEnum.STEALTH, "APPEAR"));
  }
}
