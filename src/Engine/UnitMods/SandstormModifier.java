package Engine.UnitMods;

import CommandingOfficers.Modifiers.UnitIndirectRangeModifier;
import Terrain.Environment.Weathers;
import Units.UnitContext;

public class SandstormModifier extends UnitModFilter
{
  private static final long serialVersionUID = 1L;

  public SandstormModifier()
  {
    super(new UnitIndirectRangeModifier(-1));
  }

  @Override
  public boolean shouldApplyTo(UnitContext uc)
  {
    if( uc.env != null )
      return uc.env.weatherType == Weathers.SANDSTORM;
    return false;
  }

}
