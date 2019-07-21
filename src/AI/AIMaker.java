package AI;

import CommandingOfficers.Commander;
import Engine.GameInstance;

public interface AIMaker
{
  public AIController create(Commander co);
  public String getName();
  public String getDescription();
}