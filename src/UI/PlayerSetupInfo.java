package UI;

import java.awt.Color;
import java.util.ArrayList;

import AI.AIMaker;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;
import UI.UIUtils.Faction;

/**
 * Stores state to setup Commanders with during game setup.
 * Stores current CO, color, team, AI, and faction.
 */
public class PlayerSetupInfo extends OptionSelector
{
  public enum OptionList { COMMANDER, COLOR, FACTION, TEAM, AI };

  final CommanderInfo[] COTypes;
  final OptionSelector currentCO;

  final Color[] colors;
  final OptionSelector currentColor;

  final Faction[] factions;
  final OptionSelector currentFaction;

  final OptionSelector currentTeam;

  final AIMaker[] AIs;
  final OptionSelector currentAI;

  public PlayerSetupInfo(int numPlayers, int thisPlayer, ArrayList<CommanderInfo> COTypeList, Color[] colorList, Faction[] factionList, ArrayList<AIMaker> AIList)
  {
    super(OptionList.values().length);
    
    COTypes = COTypeList.toArray(new CommanderInfo[0]);
    currentCO = new OptionSelector(COTypes.length);
    currentCO.setSelectedOption(0);
    // Copied TODO: Consider changing this to sequential or random COs once we have enough.
    // It might be better to save the last state in which a game was started, and use that.

    colors = colorList;
    currentColor = new OptionSelector(colors.length);
    currentColor.setSelectedOption(thisPlayer);

    factions = factionList;
    currentFaction = new OptionSelector(factions.length);
    currentFaction.setSelectedOption(thisPlayer);

    currentTeam = new OptionSelector(numPlayers);
    currentTeam.setSelectedOption(0); // 0 should be interpreted as "no team"

    AIs = AIList.toArray(new AIMaker[0]);
    currentAI = new OptionSelector(AIs.length);
    currentAI.setSelectedOption(0); // default to human
  }

  /**
   * Keeps the selected option within the normal bounds, and tells you if you're hitting the end of the option set.
   * @return whether you've hit the option "barrier"
   */
  public boolean pickOption(InputAction action)
  {
    int previous = this.getSelectionNormalized();
    this.handleInput(action);
    
    // If our absolute and normalized selections are no longer consistent, clamp and notify.
    if( this.getSelectionAbsolute() != this.getSelectionNormalized() )
    {
      this.setSelectedOption(previous);
      return true;
    }
    
    return false;
  }

  public OptionSelector getCurrentOptionSelector()
  {
    switch (OptionList.values()[this.getSelectionNormalized()])
    {
      case COMMANDER:
        return currentCO;
      case COLOR:
        return currentColor;
      case FACTION:
        return currentFaction;
      case TEAM:
        return currentTeam;
      case AI:
        return currentAI;
      default:
        return currentCO; // just pretend there's nothing weird going on, and hope nobody notices
    }
  }
  
  public CommanderInfo getCurrentCO()
  {
    return COTypes[currentCO.getSelectionNormalized()];
  }
  
  public Color getCurrentColor()
  {
    return colors[currentColor.getSelectionNormalized()];
  }
  
  public Faction getCurrentFaction()
  {
    return factions[currentFaction.getSelectionNormalized()];
  }
  
  public int getCurrentTeam()
  {
    return currentTeam.getSelectionNormalized() - 1; // -1 means "no team"
  }
  
  public AIMaker getCurrentAI()
  {
    return AIs[currentAI.getSelectionNormalized()];
  }

  public Commander makeCommander()
  {
    Commander co = getCurrentCO().create();

    co.myColor = getCurrentColor();
    co.faction = getCurrentFaction();
    
    co.team = getCurrentTeam();
    
    co.setAIController(getCurrentAI().create(co));

    return co;
  }
}
