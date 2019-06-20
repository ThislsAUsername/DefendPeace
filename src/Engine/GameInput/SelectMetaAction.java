package Engine.GameInput;

public class SelectMetaAction extends GameInputState<SelectMetaAction.MetaAction>
{
  enum MetaAction
  {
    CO_STATS, CO_INFO, SAVE_GAME, CO_ABILITY, QUIT_GAME, END_TURN
  }
  private static final MetaAction[] NO_ABILITY = {MetaAction.CO_STATS, MetaAction.CO_INFO, MetaAction.SAVE_GAME, MetaAction.QUIT_GAME, MetaAction.END_TURN};

  public SelectMetaAction(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    OptionSet metaActions = null;
    if( !myStateData.commander.getReadyAbilities().isEmpty() )
    {
      metaActions = new OptionSet(MetaAction.values());
    }
    else
    {
      // No CO_Ability available.
      metaActions = new OptionSet(NO_ABILITY);
    }

    return metaActions;
  }

  @Override
  public GameInputState<?> select(SelectMetaAction.MetaAction option)
  {
    GameInputState<?> next = this;
    if( MetaAction.CO_STATS == option )
    {
      next = new OpenCoStatsMenu(myStateData);
    }
    if( MetaAction.CO_INFO == option )
    {
      next = new OpenCoInfoMenu(myStateData);
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
    else if( MetaAction.END_TURN == option )
    {
      next = new StartNextTurn(myStateData);
    }
    return next;
  }
}
