package Engine.GameInput;

import CommandingOfficers.Commander;
import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;

public class SelectCoSwapTarget extends GameInputState<Commander>
{
  public SelectCoSwapTarget(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    final Army army = myStateData.army;

    Commander[] swapTo = new Commander[army.cos.length - 1];
    for( int i = 0; i < swapTo.length; ++i )
      swapTo[i] = army.cos[i+1];

    return new OptionSet(swapTo);
  }

  @Override
  public GameInputState<?> select(Commander option)
  {
    GameInputState<?> next = this;

    GameAction abilityAction = new GameAction.SwapCOAction(myStateData.army, myStateData.gameMap.game.getCurrentTurn(), option);
    myStateData.actionSet = new GameActionSet(abilityAction, false);
    next = new ActionReady(myStateData);

    return next;
  }

  @Override
  public void back()
  {
    myStateData.damagePopups.clear();
  }
}
