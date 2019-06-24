package CommandingOfficers.Modifiers;

import java.io.Serializable;

import CommandingOfficers.Commander;

public interface COModifier extends Serializable
{
  public abstract void apply(Commander commander);
  public abstract void revert(Commander commander);
}
