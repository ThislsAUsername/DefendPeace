package CommandingOfficers.Modifiers;

import java.util.Map;

import CommandingOfficers.Commander;
import Units.UnitModel;
import Units.MoveTypes.PerfectFlight;
import Units.MoveTypes.PerfectFloatHeavy;
import Units.MoveTypes.PerfectFloatLight;
import Units.MoveTypes.PerfectFootMech;
import Units.MoveTypes.PerfectFootStandard;
import Units.MoveTypes.PerfectTires;
import Units.MoveTypes.PerfectTread;

public class PerfectMoveModifier extends UnitRemodelModifier implements COModifier
{

  public PerfectMoveModifier()
  {
    super();
  }

  /** Add a new unit transform assignment. */
  public Map<UnitModel, UnitModel> init(Commander commander)
  {
    modelSwaps.clear();
    for( UnitModel um : commander.unitModels )
    {
      UnitModel newModel = UnitModel.clone(um);
      
      switch (newModel.chassis)
      {
        case AIR_HIGH:
        case AIR_LOW:
          newModel.propulsion = new PerfectFlight();
          break;
        case SHIP:
          newModel.propulsion = new PerfectFloatHeavy();
          break;
        case TANK:
          newModel.propulsion = new PerfectTread();
          break;
        default:
          break;
      }
      switch (newModel.type)
      {
        case INFANTRY:
          newModel.propulsion = new PerfectFootStandard();
          break;
        case MECH:
          newModel.propulsion = new PerfectFootMech();
          break;
        case RECON:
          newModel.propulsion = new PerfectTires();
          break;
        case ROCKETS:
          newModel.propulsion = new PerfectTires();
          break;
        case MOBILESAM:
          newModel.propulsion = new PerfectTires();
          break;
        case LANDER:
          newModel.propulsion = new PerfectFloatLight();
          break;
        default:
          break;
      }
      
      modelSwaps.put(um, newModel);
    }
    return modelSwaps;
  }
}
