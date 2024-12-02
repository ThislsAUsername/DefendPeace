package Engine.UnitMods;

import Units.UnitContext;

public class UnitCaptureModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int capMod;

  public UnitCaptureModifier(int capPowerPercentChange)
  {
    capMod = capPowerPercentChange;
  }

  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    uc.capturePower += capMod;
  }
}
