package Terrain;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import Engine.XYCoord;
import Units.AWBWUnits;
import Units.DoRUnits;
import Units.UnitModel;
import Units.UnitModelScheme;
import Units.UnitModelScheme.GameReadyModels;

public class MapInfo
{
  public final String mapName;
  public final TerrainType[][] terrain;
  public final XYCoord[][] COProperties;
  public final ArrayList<Map<XYCoord,String>> mapUnits;

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props)
  {
    this(name, tiles, props, new ArrayList<Map<XYCoord,String>>());
  }

  public MapInfo(String name, TerrainType[][] tiles, XYCoord[][] props, ArrayList<Map<XYCoord,String>> units)
  {
    mapName = name;
    terrain = tiles;
    COProperties = props;
    mapUnits = units;
  }

  public int getWidth()
  {
    return terrain.length;
  }

  public int getHeight()
  {
    return terrain[0].length;
  }

  public int getNumCos()
  {
    return COProperties.length;
  }

  /**
   * Determines which unit sets are valid for this map.
   */
  public UnitModelScheme[] getValidUnitModelSchemes()
  {
    ArrayList<UnitModelScheme> umsList = new ArrayList<UnitModelScheme>();
    umsList.add(new AWBWUnits());
    umsList.add(new DoRUnits());

    // Filter based on the existence of non-core units in the map
    for( UnitModelScheme scheme : umsList )
    {
      GameReadyModels models = scheme.getGameReadyModels();
      boolean schemeInvalid = false;
      
      for( Map<XYCoord, String> unitSet : mapUnits )
      {
        for( Entry<XYCoord, String> unitEntry : unitSet.entrySet() )
        {
          UnitModel model = UnitModelScheme.getModelFromString(unitEntry.getValue(), models.unitModels);
          if( model == null )
          {
            schemeInvalid = true; // This unit isn't supported in the scheme, so let the scheme know it's deprecated for this map
            break;
          }
        }
        if( schemeInvalid )
          break;
      }

      scheme.schemeValid = !schemeInvalid;
    }

    return umsList.toArray(new UnitModelScheme[0]);
  }
}
