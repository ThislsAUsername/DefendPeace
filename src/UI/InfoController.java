package UI;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameInstance;
import Engine.IController;

public interface InfoController extends IController
{
  Commander getSelectedCO();
  
  CommanderInfo getSelectedCOInfo();

  InfoPage getSelectedPage();

  /** Be wary, may return null */
  GameInstance getGame();
}