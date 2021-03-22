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
  private ArrayList<String> myStrings;
  private ArrayList<UnitModel> myUnitModels = null;
  private XYCoord myProductionLocation = null;

  @SuppressWarnings("unchecked")
  public SelectUnitProduction(StateData data, ArrayList<UnitModel> buildables, XYCoord buildLocation)
  {
    super(data);
    myUnitModels = buildables;
    myStrings = (ArrayList<String>) data.menuOptions;
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
      for( String buyable : myStrings )
      {
        if( option == buyable )
          {
          UnitModel model = myUnitModels.get(myStrings.indexOf(buyable));
          myStateData.actionSet = new GameActionSet(new GameAction.UnitProductionAction(myStateData.commander, model, myProductionLocation), false);
          next = new ActionReady(myStateData);
        }
      }
    }

    return next;
  }

  /**
   * Returns a list of strings of equal length containing the names of each unit with their respective prices.
   */
  public static ArrayList<String> buildDisplayStrings(ArrayList<UnitModel> models, XYCoord coord)
  {
    ArrayList<String> menuStrings = new ArrayList<>();
    int maxNameLength = 0;
    int maxPriceLength = 0;

    // Start by getting just the unit names.
    for(UnitModel model : models)
    {
      // Store each string and record the max length.
      String str = model.name;
      menuStrings.add( str );
      maxNameLength = Math.max(maxNameLength, str.length());
      maxPriceLength = Math.max(maxPriceLength, Integer.toString(model.getBuyCost(coord)).length());
    }

    maxNameLength++; // Add 1 for a space between unit name and price.

    // Modify each String to include the price at a set tab level.
    StringBuilder sb = new StringBuilder();
    for( int i = 0; i < models.size(); ++i, sb.setLength(0) )
    {
      // Start with the production item name.
      sb.append( menuStrings.get(i) );

      // Get the price as a string.
      String price = "";
      UnitModel model = models.get(i);
      if( null == model )
      {
        System.out.println("WARNING: null UnitModel encountered in production menu! Skipping.");
        continue;
      }
      else
      {
        price = Integer.toString(model.getBuyCost(coord));
      }

      // Find the difference between the max length and current length
      int neededSpace = maxNameLength + maxPriceLength - price.length() - sb.length();
      // Append spaces until this entry is the approved length.
      for( int j = 0; j < neededSpace; ++j )
        sb.append(" ");

      // Append the actual cost of the item.
      sb.append(price);

      // Plug the new string into the return list.
      menuStrings.set(i, sb.toString());
    }
    return menuStrings;
  }
  
}