package UI;

import Engine.OptionSelector;

public class GameOption extends OptionSelector
{
  public final String optionName;
  public final int minOption;
  public final String[] optionList;
  private int storedValue = 0;

  public GameOption(String name, String[] Options, int defaultValue)
  {
    super(Options.length);
    minOption = 0;
    optionName = name;
    setSelectedOption(defaultValue);
    optionList = Options;
  }
  public GameOption(String name, int min, int max, int defaultValue)
  {
    super(max - min);
    minOption = min;
    optionName = name;
    setSelectedOption(defaultValue);
    optionList = new String[1 + max - min];
    for( int i = 0; i <= max - min; i++ )
    {
      optionList[i] = "" + (min + i);
    }
  }
  public GameOption(String name, boolean defaultValue)
  {
    super(2); // No min/max means this is a boolean choice.
    minOption = 0;
    optionName = name;
    optionList = new String[] { "Off", "On" };
    if( defaultValue ) setSelectedOption(1);
  }
  @Override
  public int getSelectionNormalized()
  {
    return super.getSelectionNormalized() + minOption;
  }
  @Override
  public void setSelectedOption(int value)
  {
    super.setSelectedOption(value - minOption);
    storedValue = getSelectionNormalized();
  }
  public String getSettingValueText()
  {
    return optionList[getSelectionNormalized()-minOption];
  }
  public void storeCurrentValue()
  {
    storedValue = getSelectionNormalized();
  }
  public boolean isChanged()
  {
    return (storedValue != getSelectionNormalized());
  }
  public void loseChanges()
  {
    setSelectedOption(storedValue);
  }
}
