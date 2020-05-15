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
    
    setMoveCost(Weathers.CLEAR, TerrainType.GRASS, 1);

    setMoveCost(Weathers.RAIN, TerrainType.GRASS, 3); // I get to make stuff up, so why not?

    setMoveCost(Weathers.SNOW, TerrainType.GRASS, 2);

    setMoveCost(Weathers.SANDSTORM, TerrainType.GRASS, 1);
  }
}
