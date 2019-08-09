package AI;

import CommandingOfficers.Commander;

public interface AIMaker
{
  public AIController create(Commander co);
  public String getName();
  public String getDescription();
}