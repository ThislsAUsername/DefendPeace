package Engine.GameInput;

import CommandingOfficers.CommanderAbility;
import Engine.Army;
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
    return new OptionSet(myStateData.army.getReadyAbilities().toArray());
  }

  @Override
  public GameInputState<?> select(CommanderAbility option)
  {
    GameInputState<?> next = this;

    // Find the chosen ability and activate it.
    for( CommanderAbility ca : myStateData.army.getReadyAbilities() )
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
        showPreview = !myStateData.army.cos[0].gameRules.isFogEnabled;
        break;
      case Hidden_Units:
        showPreview = !myStateData.army.cos[0].gameRules.isFogEnabled;
        if( showPreview )
          for( Army army : myStateData.gameMap.game.armies )
            if( myStateData.army.isEnemy(army) )
              for( Unit unit : army.getUnits() )
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
