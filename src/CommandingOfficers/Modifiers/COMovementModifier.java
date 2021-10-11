package CommandingOfficers.Modifiers;

import Engine.UnitMods.UnitModifierWithDefaults;
import Units.UnitContext;

public class COMovementModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int rangeChange;

  public COMovementModifier()
  {
    this(1);
  }

  public COMovementModifier(int range)
  {
    rangeChange = range;
  }

  @Override
  public void modifyMovePower(UnitContext uc)
  {
    uc.movePower += rangeChange;
  }

}
