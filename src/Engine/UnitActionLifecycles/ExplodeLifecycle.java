package Engine.UnitActionLifecycles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.Utils;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.CommanderDefeatEvent;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.GameEvents.UnitDieEvent;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

/**
 * Effectively a wait, but the unit dies and deals damage to everything nearby
 * This action type requires parameters, and thus
 * cannot be represented as a static global constant.
 */
public abstract class ExplodeLifecycle
{
  public static class ExplodeFactory extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    public final int damage, range;

    public ExplodeFactory(int pDamage, int pRange)
    {
      damage = pDamage;
      range = pRange;
      shouldConfirm = true;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = movePath.getEndCoord();
      if( ignoreResident || map.isLocationEmpty(actor, moveLocation) )
      {
        return new GameActionSet(new ExplodeAction(actor, movePath, this), true); // We don't really need a target, but I want a confirm dialogue
      }
      return null;
    }

    @Override
    public String name()
    {
      return "EXPLODE";
    }
  }

  /** Effectively a WAIT, but the unit explodes at the end of it. */
  public static class ExplodeAction extends WaitLifecycle.WaitAction
  {
    private ExplodeFactory type;
    Unit actor;

    public ExplodeAction(Unit unit, Path path, ExplodeFactory pType)
    {
      super(unit, path);
      type = pType;
      actor = unit;
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue explodeEvents = super.getEvents(gameMap);

      if( explodeEvents.size() > 0 ) // if we successfully made a move action
      {
        GameEvent moveEvent = explodeEvents.peek();
        if( moveEvent.getEndPoint().equals(getMoveLocation()) ) // make sure we shouldn't be pre-empted
        {
          explodeEvents.add(new UnitDieEvent(actor)); // If you explode, you die

          HashSet<Unit> victims = findVictims(gameMap); // Find all of our unlucky participants

          explodeEvents.addFirst(new MassDamageEvent(victims, type.damage, false));
          if( actor.CO.units.size() == 1 )
          {
            // CO is out of units. Too bad.
            explodeEvents.add(new CommanderDefeatEvent(actor.CO));
          }
        }
      }
      return explodeEvents;
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap map)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      for( Unit victim : findVictims(map) )
        output.add(new DamagePopup(new XYCoord(victim.x, victim.y), actor.CO.myColor, Math.min(victim.getHP()-1, type.damage)*10 + "%"));

      return output;
    }

    public HashSet<Unit> findVictims(GameMap map)
    {
      HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
      for( XYCoord coord : Utils.findLocationsInRange(map, getMoveLocation(), type.range) )
      {
        Unit victim = map.getLocation(coord).getResident();
        if( null != victim && victim != actor ) // Since you're already dead when you explode, you can't get hurt in the explosion
        {
          victims.add(victim);
        }
      }
      return victims;
    }

    @Override
    public String toString()
    {
      return String.format("[Move %s to %s and explode]", actor.toStringWithLocation(), getMoveLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }
  } // ~ExplodeAction

  // No event, as MassDamageEvents are held in common with non-unit activities
}
