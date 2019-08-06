package Engine;

import java.util.ArrayList;

import UI.InputHandler.InputAction;

public class GameActionSet
{
  private ArrayList<GameAction> actionOptions;
  private OptionSelector selector;
  private boolean isTargetRequired;

  public GameActionSet(GameAction action, boolean requireTarget)
  {
    actionOptions = new ArrayList<GameAction>();
    actionOptions.add(action);
    selector = new OptionSelector(actionOptions.size());
    isTargetRequired = requireTarget;
  }

  public GameActionSet(ArrayList<GameAction> options)
  {
    // If we get in a list instead of a single action, we assume it could have been one of
    // many (with multiple possible targets), so we set requireTarget to true.
    this(options, true);
  }

  public GameActionSet(ArrayList<GameAction> options, boolean requireTarget)
  {
    actionOptions = options;
    selector = new OptionSelector(actionOptions.size());
    isTargetRequired = requireTarget;
  }

  public ArrayList<XYCoord> getTargetedLocations()
  {
    ArrayList<XYCoord> targets = new ArrayList<XYCoord>();

    for( GameAction ga : actionOptions )
    {
      targets.add(ga.getTargetLocation());
    }

    return targets;
  }

  public boolean isTargetRequired()
  {
    return isTargetRequired;
  }

  public GameAction getSelected()
  {
    return actionOptions.get(selector.getSelectionNormalized());
  }

  public void next()
  {
    selector.handleInput(InputAction.RIGHT);
  }

  public void prev()
  {
    selector.handleInput(InputAction.LEFT);
  }

  public ArrayList<GameAction> getGameActions()
  {
    return actionOptions;
  }

  @Override
  public String toString()
  {
    if( !actionOptions.isEmpty() )
    {
      // NOTE: Ability and Unit Production actions return null for getUnitActionType(), since they are
      // not unit actions. However, they are also never put into GameActionSets, so this is probably fine.
      return actionOptions.get(0).getType().name();
    }
    System.out.println("WARNING! Invalid GameActionSet!");
    return "INVALID";
  }
}
