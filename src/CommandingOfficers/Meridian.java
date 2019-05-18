package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.GameAction.WaitAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionType;
import Engine.XYCoord;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;
import Units.Weapons.Weapon;
import Units.Weapons.WeaponModel;

/**
 * Commander Meridian focuses on a unique brand of flexibility; she transforms her tanks and artillery into each other 
 */
public class Meridian extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Meridian");
      infoPages.add(new InfoPage(
          "While other commanders may focus on granular tactics, Meridian has taken a different line: her basic tanks and artillery are built from the same parts, and can be re-configured on the field at no cost.\n" +
          "This gives her forces extreme adaptability, and her powers hone this to a razor edge."));
      infoPages.add(new InfoPage(
          "Passive:\n" + 
          "- Tanks and artillery both cost 6500\n" +
          "- Tanks and artillery may transform into each other at no cost"));
      infoPages.add(new InfoPage(
          "Change and Flow ("+ChangeAndFlow.COST+"):\n" +
          "Gives an attack and defense boost of "+ChangeAndFlow.BASIC_BUFF+"%\n" +
          "Any unit that has transformed this turn is refreshed."));
      infoPages.add(new InfoPage(
          "Vehicular Charge ("+VehicularCharge.COST+"):\n" +
          "Gives an attack and defense boost of "+VehicularCharge.BASIC_BUFF+"%\n" +
          "Refreshes all land vehicles, but refreshed units suffer an attack and defense penalty of "+POST_REFRESH_STAT_ADJUSTMENT+"%\n"));
    }
    @Override
    public Commander create()
    {
      return new Meridian();
    }
  }

  /** A list of all the units I've refreshed and need to nerf. */
  private ArrayList<Unit> justTransformed = new ArrayList<Unit>();
  private ArrayList<Unit> toBeNerfed = new ArrayList<Unit>();
  private static final int POST_REFRESH_STAT_ADJUSTMENT = -25;

  public Meridian()
  {
    super(coInfo);

    // Meridian's tanks and arty cost the same
    UnitModel tank = getUnitModel(UnitModel.UnitEnum.TANK);
    UnitModel arty = getUnitModel(UnitModel.UnitEnum.ARTILLERY);
    tank.moneyCostAdjustment      -= 500;
    arty.moneyCostAdjustment      += 500;
    tank.possibleActions.add(new Transform(this, arty));
    arty.possibleActions.add(new Transform(this, tank));

    addCommanderAbility(new ChangeAndFlow(this));
    addCommanderAbility(new VehicularCharge(this));
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    justTransformed.clear();
    toBeNerfed.clear();
    modifyAbilityPower(40);
    return super.initTurn(map);
  }

  /**
   * Troops that have been refreshed by Meridian's bigger power get a stat nerf
   */
  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    if( toBeNerfed.contains(params.attacker) )
    {
      params.attackFactor += POST_REFRESH_STAT_ADJUSTMENT;
    }
    if( toBeNerfed.contains(params.defender) )
    {
      params.defenseFactor += POST_REFRESH_STAT_ADJUSTMENT;
    }
  }

  /**
   * Change and Flow refreshes units that have transformed this turn
   */
  private static class ChangeAndFlow extends CommanderAbility
  {
    private static final String STRONGARM_NAME = "Change and Flow";
    private static final int COST = 4;
    private static final int BASIC_BUFF = 10;
    
    CODamageModifier damageMod = null;
    CODefenseModifier defenseMod = null;
    Meridian COcast;

    ChangeAndFlow(Meridian commander)
    {
      super(commander, STRONGARM_NAME, COST);

      damageMod = new CODamageModifier(BASIC_BUFF);
      defenseMod = new CODefenseModifier(BASIC_BUFF);
      COcast = commander;
      AIFlags = 0; // The AI doesn't know how to use this, so it shouldn't try
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Grant the base firepower/defense bonus.
      myCommander.addCOModifier(damageMod);
      myCommander.addCOModifier(defenseMod);

      // Units that transformed are refreshed and able to move again.
      for( Unit unit : COcast.justTransformed )
      {
        unit.isTurnOver = false;
      }
    }
  }

  /**
   * Vehicular Charge refreshes all ground vehicles, at the cost of a stat penalty to the refreshed units
   */
  private static class VehicularCharge extends CommanderAbility
  {
    private static final String MOBILIZE_NAME = "Vehicular Charge";
    private static final int COST = 6;
    private static final int BASIC_BUFF = 10;

    CODamageModifier damageMod = null;
    CODefenseModifier defenseMod = null;
    Meridian COcast;

    VehicularCharge(Meridian commander)
    {
      super(commander, MOBILIZE_NAME, COST);

      damageMod = new CODamageModifier(BASIC_BUFF);
      defenseMod = new CODefenseModifier(BASIC_BUFF);
      COcast = commander;
      AIFlags = PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Grant the base firepower/defense bonus.
      myCommander.addCOModifier(damageMod);
      myCommander.addCOModifier(defenseMod);

      // Lastly, all land vehicles are refreshed and able to move again.
      for( Unit unit : COcast.units )
      {
        if( unit.model.chassis == UnitModel.ChassisEnum.TANK )
        {
          if (unit.isTurnOver)
            COcast.toBeNerfed.add(unit);
          unit.isTurnOver = false;
        }
      }
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  
  /** Effectively a wait, but the unit ends up as a different unit at the end of it. */
  private static class Transform implements UnitActionType
  {
    public final UnitModel destinationType;
    public final Meridian co;
    
    public Transform(Meridian user, UnitModel type)
    {
      co = user;
      destinationType = type;
    }
    
    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new TransformAction(actor, movePath, this), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return String.format("~%s", destinationType);
    }
  }

  /** Effectively a wait, but the unit ends up as a different unit at the end of it. */
  public static class TransformAction extends WaitAction
  {
    private Transform type;

    public TransformAction(Unit unit, Path path, Transform pType)
    {
      super(unit, path);
      type = pType;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue transformEvents = super.getEvents(gameMap);
      
      if( transformEvents.size() > 0 )
      {
        GameEvent moveEvent = transformEvents.peek();
        if (moveEvent.getEndPoint().equals(waitLoc))
        {
          transformEvents.add(new UnitTransformEvent(actor, type.destinationType, type.co));
        }
      }
      return transformEvents;
    }
    
    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and transform to %s]", actor.toStringWithLocation(), waitLoc, type.destinationType);
    }

    @Override
    public UnitActionType getUnitActionType()
    {
      return type;
    }
  }

  /** Effectively a wait, but the unit ends up as a different unit at the end of it. */
  public static class UnitTransformEvent implements GameEvent
  {
    private Unit unit;
    private UnitModel type;
    private Meridian user;

    public UnitTransformEvent(Unit unit, UnitModel destination, Meridian listener)
    {
      this.unit = unit;
      type = destination;
      user = listener;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public void sendToListener(GameEventListener listener)
    {
      // listener.receiveUnitDieEvent( this );
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
      
      user.justTransformed.add(unit);
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

}
