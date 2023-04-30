package UI;

import java.awt.Color;
import java.util.ArrayList;

import AI.AIMaker;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.GameScenario;
import Engine.XYCoord;
import Terrain.MapInfo;
import Terrain.TerrainType;
import UI.UIUtils.Faction;

/**
 * Stores a player's info during game setup.
 * Stores current CO, color, team, AI, and faction.
 */
public class PlayerSetupInfo
{
  public static class CODeets
  {
    int co, color, faction;
  }
  public ArrayList<CODeets> coList = new ArrayList<>();
  public boolean flipUnits;
  public int currentTeam;
  public int currentAi;

  @Override
  public String toString()
  {
    String coCount = "";
    if( coList.size() > 1 )
      coCount = "" + coList.size();
    StringBuilder coString = new StringBuilder();
    StringBuilder colors   = new StringBuilder();
    StringBuilder factions = new StringBuilder();
    // If we have more than one CO, build a parenthetical list within our list
    for( CODeets deets : coList )
    {
      coString.append(deets.co).append(" ");
      colors.  append(deets.color).append(" ");
      factions.append(deets.faction).append(" ");
    }

    return String.format("%s %s %s %s %s %s %s", coCount, coString, colors, factions, flipUnits, currentTeam, currentAi);
  }
  /** Initializes based on the schema defined by the toString() method above */
  public void initFromString(String input)
  {
    String noParens = input.replace("\\(\\)", "");
    // Throw away any starting space since it makes us barf
    while (noParens.charAt(0) == ' ')
      noParens = noParens.substring(1);
    String[] s = noParens.split("\\s+");
    int si = 0;

    coList.clear();
    int coCount = 1;
    if( s.length > si )
      coCount = Integer.valueOf(s[si++]);
    // If we wouldn't have added a count or it's too short to have everything, assume there's no prepended CO count
    if( coCount < 2 || input.length() < 11 + 6 * coCount )
    {
      coCount = 1;
      si = 0;
    }

    for(int i = 0; i < coCount; ++i)
      coList.add(new CODeets());

    for( int i = 0; i < coCount; ++i )
      if( s.length > si )
        coList.get(i).co = Integer.valueOf(s[si++]) % availableCommanders.length;
    for( int i = 0; i < coCount; ++i )
      if( s.length > si )
        coList.get(i).color = Integer.valueOf(s[si++]) % availableColors.length;
    for( int i = 0; i < coCount; ++i )
      if( s.length > si )
        coList.get(i).faction = Integer.valueOf(s[si++]) % availableFactions.length;

    if( s.length > si )
      flipUnits = Boolean.valueOf(s[si++]);
    if( s.length > si )
      currentTeam = Integer.valueOf(s[si++]);
    if( s.length > si )
      currentAi = Integer.valueOf(s[si++]) % availableAis.length;
  }

  private final CommanderInfo[] availableCommanders;
  private final Color[] availableColors;
  private final Faction[] availableFactions;
  private final AIMaker[] availableAis;

  public PlayerSetupInfo(int thisPlayer, MapInfo mi,
                         ArrayList<CommanderInfo> COTypeList,
                         Color[] colorList, Faction[] factionList,
                         ArrayList<AIMaker> AIList,
                         String savedVals)
  {
    CODeets deets = new CODeets();
    deets.co = thisPlayer % COTypeList.size();
    deets.color = thisPlayer % colorList.length;
    deets.faction = thisPlayer % factionList.length;
    coList.add(deets);
    flipUnits = false;
    for( int i = 0; i < mi.COProperties[thisPlayer].length; ++i )
    {
      XYCoord coord = mi.COProperties[thisPlayer][i];
      final TerrainType terrainType = mi.terrain[coord.xCoord][coord.yCoord];
      if( terrainType == TerrainType.HEADQUARTERS
          || terrainType == TerrainType.LAB)
      {
        flipUnits = (coord.xCoord > mi.terrain.length / 2);
        break;
      }
    }
    currentTeam = thisPlayer;
    currentAi = 0; // Default to human.

    availableCommanders = COTypeList.toArray(new CommanderInfo[0]);
    availableColors = colorList;
    availableFactions = factionList;
    availableAis = AIList.toArray(new AIMaker[0]);

    if( null != savedVals )
      try
      {
        initFromString(savedVals);
      }
      catch (Exception e)
      {
        System.out.println("Parsing failed for player "+thisPlayer+" on input: "+savedVals);
      }
  }

  public Commander[] makeCommanders(GameScenario.GameRules rules)
  {
    int coCount = coList.size();
    if( !rules.tagMode.supportsMultiCmdrSelect )
      coCount = 1;
    Commander[] cos = new Commander[coCount];
    for( int i = 0; i < coCount; ++i )
    {
      final CODeets deets = coList.get(i);
      cos[i] = availableCommanders[deets.co].create(rules);

      cos[i].myColor = availableColors[deets.color];
      final Faction sourceFaction = availableFactions[deets.faction];
      cos[i].faction = new Faction(sourceFaction.name, sourceFaction.basis);
      cos[i].faction.flip = flipUnits;
    }

    return cos;
  }

  public Color getCurrentColor() { return getCurrentColor(0); }
  public Color getCurrentColor(int index)
  {
    return availableColors[coList.get(index).color];
  }

  public ArrayList<CommanderInfo> getCurrentCOList()
  {
    ArrayList<CommanderInfo> cos = new ArrayList<>();
    for( CODeets deets : coList )
      cos.add(availableCommanders[deets.co]);
    return cos;
  }

  public CommanderInfo getCurrentCO(int index)
  {
    return availableCommanders[coList.get(index).co];
  }

  public Faction getCurrentFaction() { return getCurrentFaction(0); }
  public Faction getCurrentFaction(int index)
  {
    return availableFactions[coList.get(index).faction];
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
