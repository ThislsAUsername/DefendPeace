package Engine.UnitMods;

import Units.UnitContext;

public class UnitTypeFilter extends UnitModFilter
{
  private static final long serialVersionUID = 1L;

  public static final long SENTINEL = -1;
  public long allOf = SENTINEL, oneOf = SENTINEL, noneOf = SENTINEL;

  public UnitTypeFilter(UnitModifier effect)
  {
    super(effect);
  }

  @Override
  public boolean shouldApplyTo(UnitContext uc)
  {
    boolean ret = true;
    if( allOf != SENTINEL )
      ret &= uc.model.isAll(allOf);
    if( oneOf != SENTINEL )
      ret &= uc.model.isAny(oneOf);
    if( noneOf != SENTINEL )
      ret &= uc.model.isNone(noneOf);
    return ret;
  }

}
