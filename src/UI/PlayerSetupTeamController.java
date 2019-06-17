package UI;

import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;

public class PlayerSetupTeamController implements IController
{
  PlayerSetupInfo[] playerInfos;
  OptionSelector playerSelector;
  OptionSelector teamSelector;

  public PlayerSetupTeamController(PlayerSetupInfo[] infos, int selectedPlayer)
  {
    playerInfos = infos;
    playerSelector = new OptionSelector(playerInfos.length);
    teamSelector = new OptionSelector(playerInfos.length);
    playerSelector.setSelectedOption(selectedPlayer);
    teamSelector.setSelectedOption(playerInfos[selectedPlayer].currentTeam);
  }

  public int getHighlightedPlayer()
  {
    return playerSelector.getSelectionNormalized();
  }

  public PlayerSetupInfo getPlayerInfo(int index)
  {
    return playerInfos[index];
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case ENTER:
      case BACK:
        done = true; // Treat Enter/back the same.
        break;
      case UP:
      case DOWN:
        playerSelector.handleInput(action);
        teamSelector.setSelectedOption(playerInfos[playerSelector.getSelectionNormalized()].currentTeam);
        break;
      case LEFT:
      case RIGHT:
        teamSelector.handleInput(action);
        playerInfos[playerSelector.getSelectionNormalized()].currentTeam = teamSelector.getSelectionNormalized();
        break;
      default:
        // Do nothing.
    }
    return done;
  }
}
