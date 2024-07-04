package CommandingOfficers;

import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import Units.MoveTypes.MoveType;
import lombok.var;

public abstract class SturmValueFinders // Until such time as we find out AW1 works substantially differently
{
  public static class HPValueFinder implements IValueFinder
  {
    public boolean countHidden = true; // Flag to enable AW2 Sturm to not count damage on cloaked units even if he can see them
    public int getValue(Commander attacker, Unit unit)
    {
      int hp = unit.getHP();
      if( hp <= 1 )
        return 0;

      if( !unit.CO.isEnemy(attacker) )
        return -hp;
      if( !countHidden && unit.model.hidden )
        return 0;
      return hp;
    }
  }
  public static class CostValueFinder implements IValueFinder
  {
    public boolean countHidden = true; // Ditto above
    public int indirectMultiplier = 1;
    @Override
    public int getValue(Commander attacker, Unit unit)
    {
      int hp = unit.getHP();
      if( hp <= 1 )
        return 0;

      int value = unit.getCost() * hp / 10;
      if( unit.model.isAny(UnitModel.INDIRECT) )
        value *= indirectMultiplier;

      if( !unit.CO.isEnemy(attacker) )
        return -value;
      if( !countHidden && unit.model.hidden )
        return 0;
      return value;
    }
  }

  public static void modifyMoveType(UnitContext uc)
  {
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      final int moveCost = uc.moveType.getMoveCost(Weathers.CLEAR, terrain);
      // Non-impassable, non-teleport tiles
      if( MoveType.IMPASSABLE > moveCost && moveCost > 0 )
      {
        for( var weather : Weathers.values() )
          if( !weather.isCold )
            uc.moveType.setMoveCost(weather, terrain, 1);
      }
    }
  }
  public static class PerfectMoveModifier implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;

    @Override
    public void modifyMoveType(UnitContext uc)
    {
      SturmValueFinders.modifyMoveType(uc);
    }
  }
}
