package UI;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Terrain.MapInfo;

/**
 * Represents all of the information needed to create a GameInstance.
 */
public class GameBuilder
{
  public MapInfo mapInfo;
  public ArrayList<Commander> commanders;

  GameBuilder(MapInfo info)
  {
    mapInfo = info;
    commanders = new ArrayList<Commander>();
  }

  public void addCO(Commander co)
  {
    commanders.add(co);
  }
}
