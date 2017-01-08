package UI;

import java.util.ArrayList;

public class InGameCoAbilityMenu extends InGameMenu<String>
{
  ArrayList<String> availablePowers;
  ArrayList<Integer> currentOptions;

  public InGameCoAbilityMenu( ArrayList<String> abilities )
  {
    super( abilities );
    availablePowers = new ArrayList<String>();
  }
}
