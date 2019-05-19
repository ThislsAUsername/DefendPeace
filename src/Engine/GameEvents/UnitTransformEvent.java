package Engine.GameEvents;

import java.util.ArrayList;

import Engine.XYCoord;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;
import Units.Weapons.WeaponModel;

public class UnitTransformEvent implements GameEvent
{
  private Unit unit;
  private UnitModel oldType;
  private UnitModel type;

  public UnitTransformEvent(Unit unit, UnitModel destination)
  {
    this.unit = unit;
    oldType = unit.model;
    type = destination;
  }

  @Override
  public GameAnimation getEventAnimation(MapView mapView)
  {
    return null;
  }

  @Override
  public void sendToListener(GameEventListener listener)
  {
    listener.receiveUnitTranformEvent(unit, oldType);
  }

  @Override
  public void performEvent(MapMaster gameMap)
  {
    unit.model = type;
    ArrayList<Weapon> temp = unit.weapons;
    unit.weapons = new ArrayList<Weapon>();
    
    // Create the new weapon list
    for( WeaponModel weapType : type.weaponModels )
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
