package UI;


public class GameOptionBool extends GameOption<Boolean>
{
  public GameOptionBool(String name, boolean defaultValue)
  {
    super(name, new Boolean[] { false, true }, (defaultValue ? 1 : 0));
  }

  @Override
  public String getCurrentValueText()
  {
    boolean value = optionList.get(getSelectionNormalized());
    return (value ? "On" : "Off");
  }
}
