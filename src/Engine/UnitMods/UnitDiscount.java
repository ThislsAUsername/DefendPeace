package Engine.UnitMods;

import Units.UnitContext;

public class UnitDiscount implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;

  public int discount = 0;
  public UnitDiscount(int discount)
  {
    this.discount = discount;
  }

  @Override
  public void modifyCost(UnitContext uc)
  {
    uc.costRatio -= discount;
  }
}
