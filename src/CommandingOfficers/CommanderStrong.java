package CommandingOfficers;

import java.util.HashMap;
import java.util.Map;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import CommandingOfficers.Modifiers.UnitRemodelModifier;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.UnitModel;

public class CommanderStrong extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Strong", new instantiator());  
  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new CommanderStrong();
    }
  }
  
  private static Map<UnitModel.UnitEnum, UnitModel> highCapacityUnitModels;

  public CommanderStrong()
  {
    super(coInfo);

    // Strong allows infantry to be built from any production building.
    UnitModel infModel = getUnitModel(UnitModel.UnitEnum.INFANTRY);
    UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, infModel);
    upm.addProductionPair(TerrainType.SEAPORT, infModel);
    upm.apply(this); // Passive ability, so don't add it to the COModifier list; just apply it and forget it.

    // Set Strong up with a base damage buff and long-range APCs. These COModifiers are
    // not added to the modifiers collection so they will not be reverted.
    COModifier strongMod = new CODamageModifier(20); // Give us a nice base power boost.
    strongMod.apply(this);

    COMovementModifier moveMod = new COMovementModifier();
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.APC));
    moveMod.apply(this);

    // Define "high-capacity" transport unit models, to be swapped in by his abilities.
    if( null == highCapacityUnitModels )
    {
      UnitModel hcApc = UnitModel.clone( getUnitModel(UnitModel.UnitEnum.APC));
      UnitModel hcLander = UnitModel.clone( getUnitModel(UnitModel.UnitEnum.LANDER));
      UnitModel hcCopter = UnitModel.clone( getUnitModel(UnitModel.UnitEnum.T_COPTER));
      highCapacityUnitModels = new HashMap<UnitModel.UnitEnum, UnitModel>();
      highCapacityUnitModels.put(hcApc.type, hcApc);
      highCapacityUnitModels.put(hcLander.type, hcLander);
      highCapacityUnitModels.put(hcCopter.type, hcCopter);
      for( UnitModel um : highCapacityUnitModels.values() )
      {
        um.holdingCapacity += 1;
      }
      // TODO: other non-transport types?
    }

    addCommanderAbility(new StrongArmAbility(this));
    addCommanderAbility(new MobilizeAbility(this));
  }

  /**
   * StrongArm grants Strong a firepower bonus, additional mobility,
   * and the ability to build Bazooka units from all production buildings and the HQ.
   */
  private static class StrongArmAbility extends CommanderAbility
  {
    private static final String STRONGARM_NAME = "Strongarm";
    private static final int STRONGARM_COST = 10;
    private static final int STRONGARM_BUFF = 20;

    COModifier damageMod = null;

    StrongArmAbility(Commander commander)
    {
      super(commander, STRONGARM_NAME, STRONGARM_COST);

      damageMod = new CODamageModifier(STRONGARM_BUFF);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(damageMod);

      // Make infantry and bazookas buildable from all production buildings and the HQ.
      UnitModel infModel = myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY);
      UnitModel mechModel = myCommander.getUnitModel(UnitModel.UnitEnum.MECH);
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, mechModel);
      upm.addProductionPair(TerrainType.SEAPORT, mechModel);
      upm.addProductionPair(TerrainType.HEADQUARTERS, infModel);
      upm.addProductionPair(TerrainType.HEADQUARTERS, mechModel);
      myCommander.addCOModifier(upm);

      // Grant foot-soldiers and transports additional movement power.
      COMovementModifier moveMod = new COMovementModifier();

      // Use the high-capacity APC since we are swapping that model out for this turn.
      moveMod.addApplicableUnitModel(highCapacityUnitModels.get(UnitModel.UnitEnum.APC));
      moveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
      moveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
      myCommander.addCOModifier(moveMod);

      // Grant transports extra capacity
      UnitRemodelModifier urm = new UnitRemodelModifier();
      for( UnitModel.UnitEnum type : highCapacityUnitModels.keySet() )
      {
        urm.addUnitRemodel(myCommander.getUnitModel(type), highCapacityUnitModels.get(type));
      }
      myCommander.addCOModifier(urm);
    }
  }

  /**
   * Mobilize grants Strong a firepower bonus, additional mobility,
   * and the ability to build infantry units from any building he owns.
   */
  private static class MobilizeAbility extends CommanderAbility
  {
    private static final String MOBILIZE_NAME = "Mobilize";
    private static final int MOBILIZE_COST = 20;
    private static final int MOBILIZE_BUFF = 40;

    COModifier damageMod = null;

    MobilizeAbility(Commander commander)
    {
      super(commander, MOBILIZE_NAME, MOBILIZE_COST);

      damageMod = new CODamageModifier(MOBILIZE_BUFF);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(damageMod);

      // Make all foot-soldiers buildable from all buildings.
      UnitModel infModel = myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY);
      UnitModel mechModel = myCommander.getUnitModel(UnitModel.UnitEnum.MECH);
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.CITY, infModel);
      upm.addProductionPair(TerrainType.CITY, mechModel);
      upm.addProductionPair(TerrainType.AIRPORT, mechModel);
      upm.addProductionPair(TerrainType.SEAPORT, mechModel);
      upm.addProductionPair(TerrainType.HEADQUARTERS, infModel);
      upm.addProductionPair(TerrainType.HEADQUARTERS, mechModel);
      myCommander.addCOModifier(upm);

      // Grant foot-soldiers and transports two (2) additional movement power.
      COMovementModifier moveMod = new COMovementModifier(2);

      // Use the high-capacity APC since we are swapping that model out for this turn.
      moveMod.addApplicableUnitModel(highCapacityUnitModels.get(UnitModel.UnitEnum.APC));
      moveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
      moveMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
      myCommander.addCOModifier(moveMod);

      // Grant all transports extra cargo space, and TODO: let foot-soldiers hitch a ride on ground vehicles.
      UnitRemodelModifier urm = new UnitRemodelModifier();
      for( UnitModel.UnitEnum type : highCapacityUnitModels.keySet() )
      {
        urm.addUnitRemodel(myCommander.getUnitModel(type), highCapacityUnitModels.get(type));
      }
      myCommander.addCOModifier(urm);
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
