package UI;

import java.awt.Color;
import java.util.ArrayList;

import AI.AIMaker;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.GameScenario;
import UI.UIUtils.Faction;

/**
 * Stores a player's info during game setup.
 * Stores current CO, color, team, AI, and faction.
 */
public class PlayerSetupInfo
{
  public int currentCo;
  public int currentColor;
  public int currentFaction;
  public int currentTeam;
  public int currentAi;
  private final CommanderInfo[] availableCommanders;
  private final Color[] availableColors;
  private final Faction[] availableFactions;
  private final AIMaker[] availableAis;

  public PlayerSetupInfo(int thisPlayer, ArrayList<CommanderInfo> COTypeList, Color[] colorList, Faction[] factionList, ArrayList<AIMaker> AIList)
  {
    currentCo = thisPlayer % COTypeList.size();
    currentColor = thisPlayer % colorList.length;
    currentFaction = thisPlayer % factionList.length;
    currentTeam = thisPlayer;
    currentAi = 0; // Default to human.

    availableCommanders = COTypeList.toArray(new CommanderInfo[0]);
    availableColors = colorList;
    availableFactions = factionList;
    availableAis = AIList.toArray(new AIMaker[0]);
  }

  public Commander makeCommander(GameScenario.GameRules rules)
  {
    Commander co = availableCommanders[currentCo].create(rules);

    co.myColor = availableColors[currentColor];
    co.faction = availableFactions[currentFaction];

    co.team = currentTeam;

    co.setAIController(availableAis[currentAi].create(co));

    return co;
  }

  public Color getCurrentColor()
  {
    return availableColors[currentColor];
  }

  public CommanderInfo getCurrentCO()
  {
    return availableCommanders[currentCo];
  }

  public Faction getCurrentFaction()
  {
    return availableFactions[currentFaction];
  }

  public int getCurrentTeam()
  {
    return currentTeam;
  }

  public AIMaker getCurrentAI()
  {
    return availableAis[currentAi];
  }
}
