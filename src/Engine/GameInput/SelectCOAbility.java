package Engine.GameInput;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.GameAction;
import Engine.GameActionSet;
import UI.InputOptionsController;
import Units.Unit;

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
    boolean showPreview = true;
    switch(InputOptionsController.previewFogPowersOption.getSelectedObject())
    {
      case In_Fog:
        showPreview = !myStateData.commander.gameRules.isFogEnabled;
        break;
      case Hidden_Units:
        showPreview = !myStateData.commander.gameRules.isFogEnabled;
        if( showPreview )
          for( Commander co : myStateData.gameMap.commanders )
            if( myStateData.commander.isEnemy(co) )
              for( Unit unit : co.units )
                if( unit.model.hidden )
                {
                  showPreview = false;
                  break;
                }
        break;
      case Never:
        break;
    }
    if( showPreview )
      myStateData.damagePopups = ability.getDamagePopups(myStateData.gameMap);
    else
      myStateData.damagePopups.clear();
  }
  @Override
  public void back()
  {
    myStateData.damagePopups.clear();
  }
}
