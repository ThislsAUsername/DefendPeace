package CommandingOfficers.Modifiers;

import Engine.UnitMods.UnitModifierWithDefaults;
import Units.UnitContext;

public class COIndirectRangeModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int rangeChange;

  public COIndirectRangeModifier()
  {
    this(1);
  }

  public COIndirectRangeModifier(int range)
  {
    rangeChange = range;
  }

  @Override
  public void modifyAttackRange(UnitContext uc)
  {
    if( uc.rangeMax > 1 )
      uc.rangeMax += rangeChange;
  }

}
