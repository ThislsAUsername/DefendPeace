package Terrain.Maps;

import Terrain.TerrainType;

public class IMapBuilder
{
  // Convenient handles for the terrain types to shorten map definitions.
  protected static final TerrainType BR = TerrainType.BRIDGE;
  protected static final TerrainType CT = TerrainType.CITY;
  protected static final TerrainType PI = TerrainType.PILLAR;
  protected static final TerrainType ME = TerrainType.METEOR;
  protected static final TerrainType BK = TerrainType.BUNKER;
  protected static final TerrainType DN = TerrainType.DUNES;
  protected static final TerrainType FC = TerrainType.FACTORY;
  protected static final TerrainType AP = TerrainType.AIRPORT;
  protected static final TerrainType TA = TerrainType.TEMP_AIRPORT;
  protected static final TerrainType SP = TerrainType.SEAPORT;
  protected static final TerrainType TS = TerrainType.TEMP_SEAPORT;
  protected static final TerrainType FR = TerrainType.FOREST;
  protected static final TerrainType GR = TerrainType.GRASS;
  protected static final TerrainType HQ = TerrainType.HEADQUARTERS;
  protected static final TerrainType MT = TerrainType.MOUNTAIN;
  protected static final TerrainType RF = TerrainType.REEF;
  protected static final TerrainType RV = TerrainType.RIVER;
  protected static final TerrainType RD = TerrainType.ROAD;
  protected static final TerrainType SH = TerrainType.SHOAL;
  protected static final TerrainType TT = TerrainType.TELETILE;
  protected static final TerrainType XX = TerrainType.TELETILE;
  protected static final TerrainType SE = TerrainType.SEA;
  
  protected static final TerrainType LB = TerrainType.LAB;
  protected static final TerrainType SR = TerrainType.BUNKER; // full silo, but empty because lol
  protected static final TerrainType TW = TerrainType.DS_TOWER;
  protected static final TerrainType T4 = TerrainType.DOR_TOWER;
}