package CommandingOfficers.Modifiers;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.UnitMods.UnitModifier;

/**
 * An easy way to manage {@link UnitModifier}s in use under a {@link CommanderAbility}
 */
public class DynamicModifier implements COModifier
{
  private static final long serialVersionUID = 1L;
  private final UnitModifier mod;

  public DynamicModifier(UnitModifier mod)
  {
    super();
    this.mod = mod;
  }

  @Override
  public void applyChanges(Commander co)
  {
    co.add(mod);
  }

  @Override
  public void revertChanges(Commander co)
  {
    co.remove(mod);
  }


}
