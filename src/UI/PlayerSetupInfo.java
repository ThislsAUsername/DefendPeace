package UI;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

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
  public ArrayList<Integer> coList = new ArrayList<>();
  public int currentColor;
  public int currentFaction;
  public boolean flipUnits;
  public int currentTeam;
  public int currentAi;
  @Override
  public String toString()
  {
    StringBuilder coString = new StringBuilder();
    // If we have more than one CO, build a parenthetical list within our list
    if( coList.size() > 1 )
      coString.append("( ");
    for( int co : coList )
      coString.append(co).append(" ");
    if( coList.size() > 1 )
      coString.append(")");
    return String.format("%s %s %s %s %s %s", coString, currentColor, currentFaction, flipUnits, currentTeam, currentAi);
  }
  /** Initializes based on the schema defined by the toString() method above */
  public void initFromString(String input)
  {
    Scanner s = new Scanner(input);

    coList.clear();
    // If we found parentheses, then we've got a list to read
    final Pattern listRegex = Pattern.compile("\\(.*\\)");
    String coString = s.findInLine(listRegex);
    if( null != coString )
    {
      Scanner coScanner = new Scanner(coString);
      coScanner.skip("\\(");
      while (coScanner.hasNextInt())
      {
        coList.add(coScanner.nextInt() % availableCommanders.length);
      }
      coScanner.close();
    }
    else if( s.hasNextInt() )
      coList.add(s.nextInt() % availableCommanders.length);

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
    coList.add(thisPlayer % COTypeList.size());
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

  public Commander[] makeCommanders(GameScenario.GameRules rules)
  {
    Commander[] cos = new Commander[coList.size()];
    int coCount = coList.size();
    if( !rules.tagMode.supportsMultiCmdrSelect )
      coCount = 1;
    for( int i = 0; i < coCount; ++i )
    {
      cos[i] = availableCommanders[coList.get(i)].create(rules);

      cos[i].myColor = availableColors[currentColor];
      cos[i].faction = new Faction(availableFactions[currentFaction].name, availableFactions[currentFaction].basis);
      cos[i].faction.flip = flipUnits;
    }

    return cos;
  }

  public Color getCurrentColor()
  {
    return availableColors[currentColor];
  }

  public ArrayList<CommanderInfo> getCurrentCOList()
  {
    ArrayList<CommanderInfo> cos = new ArrayList<>();
    for( int index : coList )
      cos.add(availableCommanders[index]);
    return cos;
  }

  public CommanderInfo getCurrentCO(int index)
  {
    return availableCommanders[coList.get(index)];
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
