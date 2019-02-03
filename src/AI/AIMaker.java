package AI;

import CommandingOfficers.Commander;
import Engine.GameInstance;

public interface AIMaker
{
  public AIController create(GameInstance gi, Commander co);
  public String getName();
}