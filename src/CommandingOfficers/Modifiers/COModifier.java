package CommandingOfficers.Modifiers;

import java.io.Serializable;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import Units.UnitModel;

public interface COModifier extends Serializable
{
  public abstract void apply(Commander commander);
  public abstract void revert(Commander commander);

  /** 
   * Generic COModifier that operates on all units if it's given no specific types to operate on.
   */
  public abstract static class GenericCOModifier implements COModifier
  {
    private ArrayList<UnitModel> modelsToModify;

    /**
     * Modify a Commander's units.
     * By default it will affect all UnitModels owned by the Commander.
     * To impact only specific units, use addApplicableUnitModel to set
     * the ones that should be modified.
     */
    public GenericCOModifier()
    {
      modelsToModify = new ArrayList<UnitModel>();
    }

    /**
     * Add a UnitModel type for this modifier to affect. If no models are added
     * this way, then by default apply will affect all of the Commander's UnitModels.
     * @param model
     */
    public void addApplicableUnitModel(UnitModel model)
    {
      if( model != null )
      {
        modelsToModify.add(model);
      }
      else
      {
        System.out.println("Attempting to add null model to GenericCOModifier!");
        throw new NullPointerException(); // Make sure this oversight doesn't go unnoticed.
      }
    }

    @Override
    public final void apply(Commander commander)
    {
      if( modelsToModify.isEmpty() )
      {
        modelsToModify.addAll(commander.unitModels.values());
      }
      applyChanges(commander, modelsToModify);
    }
    protected abstract void applyChanges(Commander commander, ArrayList<UnitModel> models);

    @Override
    public final void revert(Commander commander)
    {
      revertChanges(commander, modelsToModify);
    }
    protected abstract void revertChanges(Commander commander, ArrayList<UnitModel> models);
  } // ~GenericCOModifier

}
