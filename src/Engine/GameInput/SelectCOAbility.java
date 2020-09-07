package Engine.GameInput;

import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameActionSet;

public class SelectCOAbility extends GameInputState<CommanderAbility>
{
  public SelectCOAbility(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(myStateData.commander.getReadyAbilities().toArray());
  }

  @Override
  public GameInputState<?> select(CommanderAbility option)
  {
    GameInputState<?> next = this;

    // Find the chosen ability and activate it.
    for( CommanderAbility ca : myStateData.commander.getReadyAbilities() )
    {
      if( option == ca )
      {
        GameAction abilityAction = new GameAction.AbilityAction(ca);
        myStateData.actionSet = new GameActionSet(abilityAction, false);
        next = new ActionReady(myStateData);
      }
    }

    return next;
  }

  @Override
  public void consider(CommanderAbility ability)
  {
    myStateData.damagePopups = ability.getDamagePopups(myStateData.gameMap);
  }
  @Override
  public void back()
  {
    myStateData.damagePopups.clear();
  }
}
