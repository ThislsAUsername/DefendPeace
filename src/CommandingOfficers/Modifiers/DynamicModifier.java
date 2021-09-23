package CommandingOfficers.Modifiers;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.UnitMods.UnitModList;
import Engine.UnitMods.UnitModifier;

/**
 * An easy way to manage {@link UnitModifier}s in use under a {@link CommanderAbility}
 */
public class DynamicModifier implements COModifier
{
  private static final long serialVersionUID = 1L;
  private final UnitModifier mod;
  private final ArrayList<UnitModList> modifiables = new ArrayList<UnitModList>();

  public DynamicModifier(UnitModifier mod)
  {
    super();
    this.mod = mod;
  }

  /**
   * Add a mod list for this modifier to affect. It's recommended to avoid mixing tiers (e.g. UnitModel + individual Units).
   * <p>If none are specified, the modifier applies to the Commander.
   */
  public void addApplicable(UnitModList moddable)
  {
    if( moddable != null )
    {
      modifiables.add(moddable);
    }
    else
    {
      System.out.println("Attempting to add null model to DynamicModifier!");
      throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
    }
  }
  public void addApplicable(Collection<UnitModList> moddable)
  {
    if( moddable != null )
    {
      for( UnitModList ml : moddable )
        addApplicable(ml);
    }
    else
    {
      System.out.println("Attempting to add null collection to DynamicModifier!");
      throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
    }
  }

  @Override
  public void applyChanges(Commander co)
  {
    if( modifiables.isEmpty() )
      co.add(mod);
    else
      for( UnitModList ml : modifiables )
        ml.add(mod);
  }

  @Override
  public void revertChanges(Commander co)
  {
    if( modifiables.isEmpty() )
      co.remove(mod);
    else
      for( UnitModList ml : modifiables )
        ml.remove(mod);
  }

}
