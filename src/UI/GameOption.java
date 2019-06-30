package UI;

import Engine.OptionSelector;

public class GameOption extends OptionSelector
{
  public final String optionName;
  public final int minOption;
  public final Object[] optionList;
  private int storedValue = 0;

  public GameOption(String name, Object[] Options, int defaultValue)
  {
    super(Options.length);
    minOption = 0;
    optionName = name;
    setSelectedOption(defaultValue);
    optionList = Options;
  }
  public GameOption(String name, int min, int max, int stride, int defaultValue)
  {
    super(1 + (max - min)/stride);
    minOption = 0;
    optionName = name;
    int numOptions = 1 + (max - min) / stride;
    optionList = new String[numOptions];
    for( int i = 0; i < numOptions; i++ )
    {
      int value = min + (i*stride);
      optionList[i] = "" + value;
      if( value == defaultValue )
      {
        setSelectedOption(i);
      }
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
  public Object getSelectedObject()
  {
    return optionList[getSelectionNormalized()-minOption];
  }
  public String getSettingValueText()
  {
    return optionList[getSelectionNormalized()-minOption].toString();
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
