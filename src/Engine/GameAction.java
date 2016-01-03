package Engine;

import Units.Unit;

public class GameAction
{
  public enum ActionType
  {
    INVALID, ATTACK, CAPTURE, LOAD, UNLOAD, WAIT
  };
  
  private Unit unitActor = null;
  private ActionType actionType;

  private int moveX;
  private int moveY;
  private int actX;
  private int actY;
  
  public GameAction(Unit unit)
  {
    unitActor = unit;
    actionType = ActionType.INVALID;
    moveX = -1;
    moveY = -1;
    actX = -1;
    actY = -1;
  }

  public void setActionType(ActionType type)
  {
    actionType = type;
  }
  
  public Unit getActor()
  {
    return unitActor;
  }

  public ActionType getActionType()
  {
    return actionType;
  }
  
  public void setMoveLocation(int x, int y)
  {
    moveX = x;
    moveY = y;
  }
  
  public int getMoveX()
  {
    return moveX;
  }
  public int getMoveY()
  {
    return moveY;
  }
  
  public void setActionLocation(int x, int y)
  {
    actX = x;
    actY = y;
  }
  
  public boolean isComplete()
  {
    boolean ready = false;
    if(moveX >= 0 && moveY >= 0 && actionType != ActionType.INVALID)
    {
      if(actionType == ActionType.WAIT || actionType == ActionType.LOAD || actionType == ActionType.CAPTURE)
      {
        ready = true;
      }
      else // ActionType is Attack or Unload - needs a target
      {
        if(actX >= 0 && actY >= 0)
        {
          ready = true;
        }
      }
    }
    return ready;
  }
  
  public boolean execute(GameInstance game)
  {
    switch(actionType)
    {
      case ATTACK:
        Unit unitTarget = game.gameMap.getLocation(actX, actY).getResident();
        
        if( unitTarget != null && unitActor.getDamage(unitTarget, moveX, moveY) != 0 )
        {
          unitActor.isTurnOver = true;
          game.gameMap.moveUnit(unitActor, moveX, moveY);

          boolean canCounter = unitTarget.getDamage(unitActor) != 0;
          CombatEngine.resolveCombat(unitActor, unitTarget, game.gameMap, canCounter);
          if( unitActor.HP <= 0 )
          {
            game.gameMap.removeUnit(unitActor);
            unitActor.CO.units.remove(unitActor);
          }
          if( unitTarget.HP <= 0 )
          {
            game.gameMap.removeUnit(unitTarget);
            unitTarget.CO.units.remove(unitTarget);
          }
          System.out.println("unitActor hp: " + unitActor.HP);
          System.out.println("unitTarget hp: " + unitTarget.HP);
        }
        break;
      case CAPTURE:
        unitActor.isTurnOver = true;
        game.gameMap.moveUnit(unitActor, moveX, moveY);
        unitActor.capture(game.gameMap.getLocation(unitActor.x, unitActor.y));
        break;
      case LOAD:
        Unit transport = game.gameMap.getLocation(game.getCursorX(), game.getCursorY()).getResident();
        
        if(null != transport && transport.hasCargoSpace(unitActor.model.type))
        {
          unitActor.isTurnOver = true;
          game.gameMap.removeUnit(unitActor);
          unitActor.x = -1;
          unitActor.y = -1;
          transport.heldUnits.add(unitActor);
        }
        break;
      case UNLOAD:
        // If we have cargo and the landing zone is empty, we drop the cargo.
        if( !unitActor.heldUnits.isEmpty() && null == game.gameMap.getLocation(game.getCursorX(), game.getCursorY()).getResident() )
        {
          unitActor.isTurnOver = true;
          game.gameMap.moveUnit(unitActor, moveX, moveY);
          Unit droppable = unitActor.heldUnits.remove(0); // TODO: Account for multi-Unit transports 
          game.gameMap.moveUnit(droppable, game.getCursorX(), game.getCursorY());
          droppable.isTurnOver = true;
        }
        break;
      case WAIT:
        unitActor.isTurnOver = true;
        game.gameMap.moveUnit(unitActor, moveX, moveY);
        break;
      case INVALID:
        default:
          System.out.println("Attempting to execute an invalid GameAction!");
    }
    
    return unitActor.isTurnOver;
  }
}