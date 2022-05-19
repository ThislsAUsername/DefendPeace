package AI;

import Engine.Army;

public interface AIMaker
{
  public AIController create(Army army);
  public String getName();
  public String getDescription();
}