package CommandingOfficers.Modifiers;

import Engine.UnitMods.UnitModifierWithDefaults;
import Units.UnitContext;

public class UnitMovementModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int rangeChange;

  public UnitMovementModifier()
  {
    this(1);
  }

  public UnitMovementModifier(int range)
  {
    rangeChange = range;
  }

  @Override
  public void modifyMovePower(UnitContext uc)
  {
    uc.movePower += rangeChange;
  }

}
