package UI;

import java.util.ArrayList;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Army;
import Engine.GameInstance;
import Engine.IController;

public interface InfoController extends IController
{
  Army getSelectedArmy();

  int getShiftDown();

  int getPageListCount();
  ArrayList<InfoPage> getSelectedPages();

  /** Be wary, may return null */
  GameInstance getGame();
}