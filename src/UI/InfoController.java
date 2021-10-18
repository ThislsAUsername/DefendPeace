package UI;

import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.GameInstance;
import Engine.IController;

public interface InfoController extends IController
{
  Commander getSelectedCO();
  
  CommanderInfo getSelectedCOInfo();

  int getShiftDown();

  ArrayList<InfoPage> getSelectedPages();

  /** Be wary, may return null */
  GameInstance getGame();
}