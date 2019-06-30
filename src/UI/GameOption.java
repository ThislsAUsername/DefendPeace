package UI;

import java.util.ArrayList;

import Engine.OptionSelector;

public class GameOption<T> extends OptionSelector
{
  public final String optionName;
  public final int minOption;
  public final ArrayList<T> optionList;
  private int storedValue = 0;

  public GameOption(String name, T[] Options, int defaultIndex)
  {
    super(Options.length);
    minOption = 0;
    optionName = name;
    setSelectedOption(defaultIndex);
    optionList = new ArrayList<T>();
    for( T opt : Options ) optionList.add(opt);
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
  public T getSelectedObject()
  {
    return optionList.get(getSelectionNormalized()-minOption);
  }
  public String getSettingValueText()
  {
    return optionList.get(getSelectionNormalized()-minOption).toString();
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
