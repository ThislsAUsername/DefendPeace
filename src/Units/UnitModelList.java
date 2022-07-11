package Units;

import java.util.ArrayList;

public class UnitModelList extends ArrayList<UnitModel>
{
  private static final long serialVersionUID = 1L;

  public UnitModel getUnitModel(long unitRole)
  {
    return getUnitModel(unitRole, true);
  }
  public UnitModel getUnitModel(long unitRole, boolean matchOnAny)
  {
    UnitModel um = null;

    for( UnitModel iter : this )
    {
      boolean some = iter.isAny(unitRole);
      boolean all = iter.isAll(unitRole);
      if( all || (some && matchOnAny) )
      {
        um = iter;
        break;
      }
    }

    return um;
  }

  public ArrayList<UnitModel> getAllModels(long unitRole)
  {
    return getAllModels(unitRole, true);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny)
  {
    return getAllModels(unitRole, matchOnAny, 0);
  }
  public ArrayList<UnitModel> getAllModelsNot(long excludedUnitRoles)
  {
    long allFlags = ~0;
    return getAllModels(allFlags, true, excludedUnitRoles);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny, long excludedRoles)
  {
    ArrayList<UnitModel> models = new ArrayList<UnitModel>();

    for( UnitModel iter : this )
    {
      boolean some = iter.isAny(unitRole) && iter.isNone(excludedRoles);
      boolean all = iter.isAll(unitRole) && iter.isNone(excludedRoles);
      if( all || (some && matchOnAny) )
      {
        models.add(iter);
      }
    }

    return models;
  }
}
