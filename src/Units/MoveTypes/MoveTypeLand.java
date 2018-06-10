package Units.MoveTypes;

import Terrain.Environment.Weathers;

/** MoveTypeLand has a move cost of 1 for all land-based terrain types. */
public class MoveTypeLand extends MoveType
{
  public MoveTypeLand()
  {
    // The superclass sets all costs to 99; we just need to set the land-based terrains to 1.
    moveCosts.get(Weathers.CLEAR).setAllLandCosts(1);
    moveCosts.get(Weathers.RAIN).setAllLandCosts(1);
    moveCosts.get(Weathers.SNOW).setAllLandCosts(1);
    moveCosts.get(Weathers.SANDSTORM).setAllLandCosts(1);
  }
}
