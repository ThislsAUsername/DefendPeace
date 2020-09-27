package Engine.GameInput;

public class ConfirmUnitAction extends GameInputState<ConfirmUnitAction.ConfirmActionEnum>
{
  enum ConfirmActionEnum
  {
    CANCEL, CONFIRM
  };

  public ConfirmUnitAction(StateData data)
  {
    super(data);
  }

  @Override
  protected OptionSet initOptions()
  {
    return new OptionSet(ConfirmActionEnum.values());
  }

  @Override
  public GameInputState<?> select(ConfirmUnitAction.ConfirmActionEnum option)
  {
    GameInputState<?> next = this;
    switch(option)
    {
      case CANCEL:
        next = null;
        break;
      case CONFIRM:
        next = new ActionReady(myStateData);
        break;
    }
    
    return next;
  }

  @Override
  public void consider(ConfirmUnitAction.ConfirmActionEnum confirm)
  {
    if( confirm == ConfirmActionEnum.CONFIRM )
      myStateData.damagePopups = myStateData.actionSet.getSelected().getDamagePopups(myStateData.gameMap);
    else
      myStateData.damagePopups.clear();
  }
  @Override
  public void back()
  {
    myStateData.damagePopups.clear();
  }
}
