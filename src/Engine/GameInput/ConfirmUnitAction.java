package Engine.GameInput;

import java.util.Collection;

import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Terrain.GameMap;

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
  public Collection<DamagePopup> getDamagePopups(GameMap map, XYCoord target)
  {
    return myStateData.actionSet.getSelected().getDamagePopups(map);
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
}
