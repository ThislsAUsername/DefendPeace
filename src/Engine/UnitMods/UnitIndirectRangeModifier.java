package Engine.UnitMods;

import Units.UnitContext;

public class UnitIndirectRangeModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int rangeChange;

  public UnitIndirectRangeModifier()
  {
    this(1);
  }

  public UnitIndirectRangeModifier(int range)
  {
    rangeChange = range;
  }

  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.weapon != null && uc.weapon.rangeMax() > 1 )
      uc.rangeMax += rangeChange;
  }

}
