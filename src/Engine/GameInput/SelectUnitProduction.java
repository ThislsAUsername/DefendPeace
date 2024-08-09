package Engine.GameInput;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.XYCoord;
import UI.InGameMenu;
import UI.InGameMenu.MenuOption;
import Units.UnitModel;

/************************************************************
 * Presents options for building a unit.                    *
 ************************************************************/
class SelectUnitProduction extends GameInputState<Engine.GameInput.SelectUnitProduction.UnitBuildOption>
{
  private Commander builder;
  private ArrayList<UnitModel> myUnitModels = null;
  private XYCoord myProductionLocation = null;

  public SelectUnitProduction(StateData data, Commander builder, ArrayList<UnitModel> buildables, XYCoord buildLocation)
  {
    super(data);
    this.builder = builder;
    myUnitModels = buildables;
    myProductionLocation = buildLocation;
  }

  @Override
  protected OptionSet initOptions()
  {
    OptionSet options = null;
    if( null != myStateData.menuOptions )
      options = new OptionSet(myStateData.menuOptions.toArray());
    return options;
  }

  @Override
  public GameInputState<?> select(UnitBuildOption option)
  {
    GameInputState<?> next = this;

    if( null != option && option.enabled && null != myUnitModels )
    {
      for( UnitModel buyable : myUnitModels )
      {
        if( option.item == buyable )
        {
          myStateData.actionSet = new GameActionSet(new GameAction.UnitProductionAction(builder, buyable, myProductionLocation), false);
          next = new ActionReady(myStateData);
        }
      }
    }

    return next;
  }
  @SuppressWarnings("unchecked")
  @Override
  public InGameMenu<? extends Object> getMenu()
  {
    return new InGameMenu<UnitModel>((ArrayList<MenuOption<UnitModel>>)myStateData.menuOptions, getOptionSelector(), true);
  }

  public static class UnitBuildOption extends MenuOption<UnitModel>
  {
    public String label = "";
    public int price;
    public UnitBuildOption(UnitModel um, int price)
    {
      super(um);
      this.price = price;
    }
    @Override
    public String toString()
    {
      return label;
    }
    public void bakeLabel(int nameLen, int priceLen)
    {
      String fmt = "%-"+nameLen+"s %"+priceLen+"d";
      label = String.format(fmt, item.name, price);
    }
  }

  /**
   * Returns a list of strings of equal length containing the names of each unit with their respective prices.
   */
  public static ArrayList<UnitBuildOption> buildDisplayStrings(Commander co, ArrayList<UnitModel> models, XYCoord coord)
  {
    ArrayList<UnitBuildOption> menuOptions = new ArrayList<>();
    int maxNameLength = 0;
    int maxPriceLength = 0;
    int armyCount = 0;
    boolean disableAll = false;
    for( Commander coi : co.army.cos )
      armyCount += coi.units.size();
    if( co.gameRules.unitCap <= armyCount )
      disableAll = true;

    // Start by getting just the unit names.
    for(UnitModel model : models)
    {
      int price = co.getBuyCost(model, coord);
      menuOptions.add(new UnitBuildOption(model, price));
      maxNameLength  = Math.max(maxNameLength, model.name.length());
      maxPriceLength = Math.max(maxPriceLength, Integer.toString(price).length());
    }

    for( UnitBuildOption ubo : menuOptions )
    {
      if( ubo.price > co.army.money || disableAll )
        ubo.enabled = false;
      ubo.bakeLabel(maxNameLength, maxPriceLength);
    }

    return menuOptions;
  }
  
}