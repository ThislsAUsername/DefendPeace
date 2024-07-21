package Engine.UnitMods;

import Units.UnitContext;

public class InstaCapModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    uc.capturePower += 1900; // Technically 20.5x with Sami's D2D buff
  }
}
