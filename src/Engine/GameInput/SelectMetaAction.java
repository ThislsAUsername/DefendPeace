package Engine.GameInput;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Army;
import Engine.GameInput.GameInputHandler.InputType;

public class SelectMetaAction extends GameInputState<SelectMetaAction.MetaAction>
{
  enum MetaAction
  {
    CO_STATS, CO_INFO, DAMAGE_CHART, SAVE_GAME, CO_ABILITY, QUIT_GAME, SWAP, END_TURN
  }
  // Four different cases isn't great, but I like the relative simplicity for now
  // Will probably just make a pruned copy on-the-fly if we end up needing more cases
  private static final MetaAction[] DEFAULT      = {MetaAction.CO_STATS, MetaAction.CO_INFO, MetaAction.DAMAGE_CHART, MetaAction.SAVE_GAME, MetaAction.QUIT_GAME, MetaAction.END_TURN};
  private static final MetaAction[] ONLY_ABILITY = {MetaAction.CO_STATS, MetaAction.CO_INFO, MetaAction.DAMAGE_CHART, MetaAction.SAVE_GAME, MetaAction.CO_ABILITY, MetaAction.QUIT_GAME, MetaAction.END_TURN};
  private static final MetaAction[] ONLY_SWAP    = {MetaAction.CO_STATS, MetaAction.CO_INFO, MetaAction.DAMAGE_CHART, MetaAction.SAVE_GAME, MetaAction.QUIT_GAME, MetaAction.SWAP, MetaAction.END_TURN};

  public SelectMetaAction(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    OptionSet metaActions = null;
    final Army army = myStateData.army;
    final boolean abilityReady = !army.getReadyAbilities().isEmpty();
    final boolean canSwap = army.gameRules.tagMode.supportsMultiCmdrSelect && (army.cos.length > 1);
    if( abilityReady && canSwap )
    {
      metaActions = new OptionSet(MetaAction.values());
    }
    else if( abilityReady )
    {
      metaActions = new OptionSet(ONLY_ABILITY);
    }
    else if( canSwap )
    {
      metaActions = new OptionSet(ONLY_SWAP);
    }
    else
    {
      metaActions = new OptionSet(DEFAULT);
    }

    return metaActions;
  }

  @Override
  public GameInputState<?> select(SelectMetaAction.MetaAction option)
  {
    GameInputState<?> next = this;
    if( MetaAction.CO_STATS == option )
    {
      TerminalEnumState.state = InputType.CO_STATS;
      next = new TerminalEnumState(myStateData);
    }
    if( MetaAction.CO_INFO == option )
    {
      TerminalEnumState.state = InputType.CO_INFO;
      next = new TerminalEnumState(myStateData);
    }
    if( MetaAction.DAMAGE_CHART == option )
    {
      TerminalEnumState.state = InputType.DAMAGE_CHART;
      next = new TerminalEnumState(myStateData);
    }
    else if( MetaAction.QUIT_GAME == option )
    {
      next = new ConfirmExit(myStateData);
    }
    else if( MetaAction.SAVE_GAME == option )
    {
      next = new SaveGame(myStateData);
    }
    else if( MetaAction.CO_ABILITY == option )
    {
      next = new SelectCOAbility(myStateData);
    }
    else if( MetaAction.SWAP == option )
    {
      next = new SelectCoSwapTarget(myStateData);
    }
    else if( MetaAction.END_TURN == option )
    {
      GameAction endTurn = new GameAction.EndTurnAction(myStateData.army, myStateData.gameMap.game.getCurrentTurn());
      GameActionSet gaSet = new GameActionSet(endTurn, false);
      myStateData.actionSet = gaSet;
      next = new ActionReady(myStateData);
    }
    return next;
  }
}
