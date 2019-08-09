package Units;

import Engine.UnitActionType;

public class SubSubModel extends SubModel
{
  private static final long serialVersionUID = 1L;
  private static final int IDLE_FUEL_BURN = 5;

  public SubSubModel()
  {
    super();
    type = UnitEnum.SUB_SUB;
    chassis = ChassisEnum.SUBMERGED;
    idleFuelBurn = IDLE_FUEL_BURN;
    hidden = true;
  }
  
  @Override
  protected void addSubAction()
  {
    possibleActions.add(new UnitActionType.Transform(UnitEnum.SUB, "RISE"));
  }
}
