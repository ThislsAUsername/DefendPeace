package Units.MoveTypes;

import Terrain.Environment.Weathers;
import Terrain.TerrainType;

/** Just like normal tires, but faster on grass/ruins */
public class TiresRugged extends Tires
{
  private static final long serialVersionUID = 1L;

  public TiresRugged()
  {
    super();
    
    setMoveCost(TerrainType.GRASS, 1);

    setMoveCost(Weathers.RAIN, TerrainType.GRASS, 2);
    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);
  }
}
