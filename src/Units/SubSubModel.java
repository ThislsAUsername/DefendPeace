package Units;

public class SubSubModel extends SubModel
{
  private static final int IDLE_FUEL_BURN = 5;

  public SubSubModel()
  {
    super();
    type = UnitEnum.SUB_SUB;
    chassis = ChassisEnum.SUBMERGED;
    idleFuelBurn = IDLE_FUEL_BURN;
  }
}
