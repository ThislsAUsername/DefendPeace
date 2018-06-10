package Terrain.Maps;

import Terrain.Types.Airport;
import Terrain.Types.BaseTerrain;
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
  protected static final BaseTerrain BR = Bridge.getInstance();
  protected static final BaseTerrain CT = City.getInstance();
  protected static final BaseTerrain DN = Dunes.getInstance();
  protected static final BaseTerrain FC = Factory.getInstance();
  protected static final BaseTerrain AP = Airport.getInstance();
  protected static final BaseTerrain SP = Seaport.getInstance();
  protected static final BaseTerrain FR = Forest.getInstance();
  protected static final BaseTerrain GR = Grass.getInstance();
  protected static final BaseTerrain HQ = Headquarters.getInstance();
  protected static final BaseTerrain LB = Lab.getInstance();
  protected static final BaseTerrain MT = Mountain.getInstance();
  protected static final BaseTerrain RF = Reef.getInstance();
  protected static final BaseTerrain RD = Road.getInstance();
  protected static final BaseTerrain SH = Shoal.getInstance();
  protected static final BaseTerrain SE = Sea.getInstance();
}