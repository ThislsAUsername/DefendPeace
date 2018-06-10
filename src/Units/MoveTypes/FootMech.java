package Units.MoveTypes;

import Terrain.Environment.Terrains;
import Terrain.Environment.Weathers;

public class FootMech extends MoveTypeLand
{
  public FootMech()
  {
    setMoveCost(Weathers.SNOW, Terrains.MOUNTAIN, 2);
    setMoveCost(Weathers.SANDSTORM, Terrains.DUNES, 2);
  }
}
