package Engine.GameInput;

import java.util.ArrayList;

import Engine.GameAction;
import Engine.GameActionSet;
import Engine.XYCoord;
import Units.UnitModel;

/************************************************************
 * Presents options for building a unit.                    *
 ************************************************************/
class SelectUnitProduction extends GameInputState<String>
{
  private static ArrayList<String> menuStrings;
  private ArrayList<UnitModel> myUnitModels = null;
  private XYCoord myProductionLocation = null;

  public SelectUnitProduction(StateData data, ArrayList<UnitModel> buildables, XYCoord buildLocation)
  {
    super(data);
    myUnitModels = buildables;
    myProductionLocation = buildLocation;
  }

  @Override
  protected OptionSet initOptions()
  {
    OptionSet options = null;
    if( null != myStateData.menuOptions )
    {
      options = new OptionSet(myStateData.menuOptions.toArray());
    }
    return options;
  }

  @Override
  public GameInputState<?> select(String option)
  {
    GameInputState<?> next = this;

    if( null != option && null != myUnitModels )
    {
      for( String buyable : menuStrings )
      {
        if( option == buyable )
          {
          UnitModel model = myUnitModels.get(menuStrings.indexOf(buyable));
          myStateData.actionSet = new GameActionSet(new GameAction.UnitProductionAction(myStateData.gameMap, myStateData.commander, model, myProductionLocation), false);
          next = new ActionReady(myStateData);
        }
      }
    }

    return next;
  }

  /**
   * After execution, menuStrings will be (re)populated with strings of equal length
   * containing the names of each unit with their respective prices.
   */
  public static ArrayList<String> buildDisplayStrings(ArrayList<UnitModel> models)
  {
    menuStrings = new ArrayList<>();
    int maxLength = 0;

    // Start by getting just the unit names.
    for(int i = 0; i < models.size(); ++i)
    {
      // Store each string and record the max length.
      String str = models.get(i).type.toString();
      menuStrings.add( str );
      maxLength = (str.length() > maxLength) ? str.length() : maxLength;
    }

    maxLength++; // Add 1 for a space between unit name and price.

    // Modify each String to include the price at a set tab level.
    StringBuilder sb = new StringBuilder();
    for( int i = 0; i < models.size(); ++i, sb.setLength(0) )
    {
      // Start with the production item name.
      sb.append( menuStrings.get(i) );

      // Append spaces until this entry is the approved length.
      for( ; sb.length() < maxLength; sb.append(" ") );

      // Get the price as a string.
      int price = 0;
      UnitModel model = models.get(i);
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
    return menuStrings;
  }
  
}