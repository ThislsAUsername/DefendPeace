package Terrain.Maps;

import Terrain.Types.Airport;
import Terrain.Types.TerrainType;
import Terrain.Types.Bridge;
import Terrain.Types.City;
import Terrain.Types.Dunes;
import Terrain.Types.Factory;
import Terrain.Types.Forest;
import Terrain.Types.Grass;
import Terrain.Types.Headquarters;
import Terrain.Types.Lab;
import Terrain.Types.Mountain;
import Terrain.Types.Reef;
import Terrain.Types.Road;
import Terrain.Types.Sea;
import Terrain.Types.Seaport;
import Terrain.Types.Shoal;

public class IMapBuilder
{
  // Convenient handles for the terrain types to shorten map definitions.
  protected static final TerrainType BR = Bridge.getInstance();
  protected static final TerrainType CT = City.getInstance();
  protected static final TerrainType DN = Dunes.getInstance();
  protected static final TerrainType FC = Factory.getInstance();
  protected static final TerrainType AP = Airport.getInstance();
  protected static final TerrainType SP = Seaport.getInstance();
  protected static final TerrainType FR = Forest.getInstance();
  protected static final TerrainType GR = Grass.getInstance();
  protected static final TerrainType HQ = Headquarters.getInstance();
  protected static final TerrainType LB = Lab.getInstance();
  protected static final TerrainType MT = Mountain.getInstance();
  protected static final TerrainType RF = Reef.getInstance();
  protected static final TerrainType RD = Road.getInstance();
  protected static final TerrainType SH = Shoal.getInstance();
  protected static final TerrainType SE = Sea.getInstance();
}