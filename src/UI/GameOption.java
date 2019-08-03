package UI;

import java.util.ArrayList;

import Engine.OptionSelector;

public class GameOption<T> extends OptionSelector
{
  public final String optionName;
  public final ArrayList<T> optionList;
  private int storedValue = 0;

  public GameOption(String name, T[] Options, int defaultIndex)
  {
    super(Options.length);
    optionName = name;
    setSelectedOption(defaultIndex);
    optionList = new ArrayList<T>();
    for( T opt : Options ) optionList.add(opt);
  }
  @Override
  public int getSelectionNormalized()
  {
    return super.getSelectionNormalized();
  }
  @Override
  public void setSelectedOption(int value)
  {
    super.setSelectedOption(value);
    storedValue = getSelectionNormalized();
  }
  public T getSelectedObject()
  {
    return optionList.get(getSelectionNormalized());
  }
  public String getCurrentValueText()
  {
    return optionList.get(getSelectionNormalized()).toString();
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
