package UI;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Scanner;

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
  public boolean flipUnits;
  public int currentTeam;
  public int currentAi;
  @Override
  public String toString()
  {
    return String.format("%s %s %s %s %s %s", currentCo, currentColor, currentFaction, flipUnits, currentTeam, currentAi);
  }
  /** Initializes based on the schema defined by the toString() method above */
  public void initFromString(String input)
  {
    Scanner s = new Scanner(input);
    if( s.hasNextInt())
      currentCo = s.nextInt() % availableCommanders.length;
    if( s.hasNextInt())
      currentColor = s.nextInt() % availableColors.length;
    if( s.hasNextInt())
      currentFaction = s.nextInt() % availableFactions.length;
    if( s.hasNextBoolean() )
      flipUnits = s.nextBoolean();
    if( s.hasNextInt())
      currentTeam = s.nextInt();
    if( s.hasNextInt())
      currentAi = s.nextInt() % availableAis.length;
    s.close();
  }
  private final CommanderInfo[] availableCommanders;
  private final Color[] availableColors;
  private final Faction[] availableFactions;
  private final AIMaker[] availableAis;

  public PlayerSetupInfo(int thisPlayer,
                         ArrayList<CommanderInfo> COTypeList,
                         Color[] colorList, Faction[] factionList,
                         ArrayList<AIMaker> AIList,
                         String savedVals)
  {
    currentCo = thisPlayer % COTypeList.size();
    currentColor = thisPlayer % colorList.length;
    currentFaction = thisPlayer % factionList.length;
    flipUnits = 0 < (thisPlayer % 2);
    currentTeam = thisPlayer;
    currentAi = 0; // Default to human.

    availableCommanders = COTypeList.toArray(new CommanderInfo[0]);
    availableColors = colorList;
    availableFactions = factionList;
    availableAis = AIList.toArray(new AIMaker[0]);

    if( null != savedVals )
      initFromString(savedVals);
  }

  public Commander makeCommander(GameScenario.GameRules rules)
  {
    Commander co = availableCommanders[currentCo].create(rules);

    co.myColor = availableColors[currentColor];
    co.faction = new Faction(availableFactions[currentFaction].name, availableFactions[currentFaction].basis);
    co.faction.flip = flipUnits;

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
