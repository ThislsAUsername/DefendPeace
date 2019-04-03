package UI;

import CommandingOfficers.Commander;
import CommandingOfficers.COMaker.InfoPage;
import Engine.GameInstance;
import Engine.IController;

public interface InfoController extends IController
{
  Commander getSelectedCO();

  InfoPage getSelectedPage();

  /** Be wary, may return null */
  GameInstance getGame();
}