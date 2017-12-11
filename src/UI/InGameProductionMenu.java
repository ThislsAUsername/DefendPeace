package UI;

import java.util.ArrayList;

import Units.UnitModel;

/**
 * Overrides functionality in the base class to include prices
 * along with unit names in production menus.
 */
public class InGameProductionMenu extends InGameMenu<UnitModel>
{
  ArrayList<String> menuStrings;

  public InGameProductionMenu( ArrayList<UnitModel> options )
  {
    super( options );
    menuStrings = new ArrayList<String>();
    buildDisplayStrings();
  }

  @Override
  public void resetOptions(ArrayList<UnitModel> newOptions)
  {
    super.resetOptions(newOptions);
    buildDisplayStrings();
  }

  @Override
  public String getOptionString( int index )
  {
    return menuStrings.get(index);
  }

  /**
   * After execution, menuStrings will be (re)populated with strings of equal length
   * containing the names of each unit with their respective prices.
   */
  private void buildDisplayStrings()
  {
    menuStrings.clear();
    int maxLength = 0;

    // Start by getting just the unit names.
    for(int i = 0; i < getNumOptions(); ++i)
    {
      // Store each string and record the max length.
      String str = getOption(i).type.toString();
      menuStrings.add( str );
      maxLength = (str.length() > maxLength) ? str.length() : maxLength;
    }

    maxLength++; // Add 1 for a space between unit name and price.

    // Modify each String to include the price at a set tab level.
    StringBuilder sb = new StringBuilder();
    for( int i = 0; i < getNumOptions(); ++i, sb.setLength(0) )
    {
      // Start with the production item name.
      sb.append( menuStrings.get(i) );

      // Append spaces until this entry is the approved length.
      for( ; sb.length() < maxLength; sb.append(" ") );

      // Get the price as a string.
      int price = 0;
      UnitModel model = getOption(i);
      if( null == model )
      {
        System.out.println("WARNING: null UnitModel encountered in production menu! Skipping.");
        continue;
      }
      else
      {
        price = model.moneyCost;
      }

      // Pad the price with an extra space if it is only four digits.
      // NOTE: This line assumes that all prices will be either four or five digits.
      if( price < 10000 )
      {
        sb.append(" ");
      }

      // Append the actual cost of the item.
      sb.append( Integer.toString(price) );

      // Plug the new string into the return list.
      menuStrings.set(i, sb.toString());
    }
  }
}
