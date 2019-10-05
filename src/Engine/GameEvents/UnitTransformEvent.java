package Engine.GameEvents;

import java.util.ArrayList;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;
import Units.Weapon;
import Units.WeaponModel;

public class UnitTransformEvent implements GameEvent
{
  private Unit unit;
  private UnitModel oldType;
  private UnitModel destinationType;

  public UnitTransformEvent(Unit unit, UnitModel destination)
  {
    this.unit = unit;
    oldType = unit.model;
    destinationType = destination;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveUnitTransformEvent(unit, oldType);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    unit.model = destinationType;
    ArrayList<Weapon> temp = unit.weapons;
    unit.weapons = new ArrayList<Weapon>();
    
    // Create the new weapon list
    for( WeaponModel weapType : destinationType.weaponModels )
    {
      unit.weapons.add(new Weapon(weapType));
    }

    // Try not to create ammo from nothing
    for( int i = 0; i < temp.size() && i < unit.weapons.size(); i++ )
    {
      Weapon weap = unit.weapons.get(i);
      if( !weap.model.hasInfiniteAmmo )
        weap.ammo = temp.get(i).ammo;
    }
  }

  @Override
  public XYCoord getStartPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }

  @Override
  public XYCoord getEndPoint()
  {
    return new XYCoord(unit.x, unit.y);
  }
}
