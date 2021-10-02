package Engine.UnitMods;

import Units.UnitContext;

public class UnitDiscount implements UnitModifier
{
  public double discount = 0;
  public UnitDiscount(double discount)
  {
    this.discount = discount;
  }

  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costMultiplier -= discount;
  }
}
