package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Commander;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.UnitModel;
import Units.MoveTypes.MoveType;

public class PerfectMoveModifier extends GenericUnitModifier
{
  private static final long serialVersionUID = 1L;

  Map<UnitModel, MoveType> originalPropulsions = new HashMap<UnitModel, MoveType>();

  public final boolean doSnow;
  public PerfectMoveModifier(boolean zoomThroughSnow)
  {
    doSnow = zoomThroughSnow;
  }
  public PerfectMoveModifier()
  {
    this(false);
  }

  @Override
  protected final void modifyUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      originalPropulsions.put(um,um.propulsion);
      MoveType newGroove = new MoveType(um.propulsion);
      um.propulsion = newGroove;

      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        if( newGroove.getMoveCost(Weathers.CLEAR, terrain) < 99 )
        {
          newGroove.setMoveCost(Weathers.CLEAR, terrain, 1);
          newGroove.setMoveCost(Weathers.RAIN, terrain, 1);
          if (doSnow)
            newGroove.setMoveCost(Weathers.SNOW, terrain, 1);
        }
      }
    }
  }

  @Override
  protected final void restoreUnits(Commander commander, ArrayList<UnitModel> models)
  {
    for( UnitModel um : models )
    {
      um.propulsion = originalPropulsions.get(um);
    }
  }
}
