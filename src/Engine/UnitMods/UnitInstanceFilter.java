package Engine.UnitMods;

import java.util.ArrayList;

import Units.Unit;
import Units.UnitContext;

public class UnitInstanceFilter extends UnitModFilter
{
  private static final long serialVersionUID = 1L;

  public final ArrayList<Unit> instances = new ArrayList<>();

  public UnitInstanceFilter(UnitModifier effect)
  {
    super(effect);
  }

  @Override
  public boolean shouldApplyTo(UnitContext uc)
  {
    return instances.contains(uc.unit);
  }

}
