package Terrain.Types;


public abstract class Ownable extends Grass
{

  protected Ownable()
  {
    capturable = true;
    isCover = true; // DoR properties give cover in FoW
    // comm towers and Radar don't give income, so that's separate
    baseIndex = Grass.getIndex();
  }

}
