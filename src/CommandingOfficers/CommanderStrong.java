package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.GameInstance;
import Terrain.TerrainType;
import Units.UnitModel;

public class CommanderStrong extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Strong", CommanderLibrary.CommanderEnum.STRONG);

  public CommanderStrong()
  {
    super(coInfo);

    // Strong places a high value on the human element, so he allows infantry to be built from any production building.
    unitProductionByTerrain.get(TerrainType.AIRPORT).add(getUnitModel(UnitModel.UnitEnum.INFANTRY));
    unitProductionByTerrain.get(TerrainType.SEAPORT).add(getUnitModel(UnitModel.UnitEnum.INFANTRY));
    unitProductionByTerrain.put(TerrainType.HEADQUARTERS, new ArrayList<UnitModel>());
    unitProductionByTerrain.get(TerrainType.HEADQUARTERS).add(getUnitModel(UnitModel.UnitEnum.INFANTRY));
    unitProductionByTerrain.put(TerrainType.CITY, new ArrayList<UnitModel>());

    // Set Strong up with a base damage buff and long-range APCs. These COModifiers are
    // not added to the modifiers collection so they will not be reverted.
    COModifier strongMod = new CODamageModifier(20); // Give us a nice base power boost.
    strongMod.apply(this);

    COMovementModifier moveMod = new COMovementModifier();
    moveMod.addApplicableUnitType(UnitModel.UnitEnum.APC);
    moveMod.apply(this);

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
    private static final int STRONGARM_COST = 0;
    private static final int STRONGARM_BUFF = 20;

    COModifier damageMod = null;

    StrongArmAbility(Commander commander)
    {
      super(commander, STRONGARM_NAME, STRONGARM_COST);

      damageMod = new CODamageModifier(STRONGARM_BUFF);
    }

    @Override
    protected void perform(GameInstance game)
    {
      myCommander.addCOModifier(damageMod);

      // Make bazookas buildable from all production buildings.
//      myCommander.unitProductionByTerrain.get(TerrainType.AIRPORT).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
//      myCommander.unitProductionByTerrain.get(TerrainType.SEAPORT).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
//      myCommander.unitProductionByTerrain.get(TerrainType.HEADQUARTERS).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));

      // Grant foot-soldiers and transports additional movement power.
      COMovementModifier moveMod = new COMovementModifier();
      moveMod.addApplicableUnitType(UnitModel.UnitEnum.APC);
      moveMod.addApplicableUnitType(UnitModel.UnitEnum.INFANTRY);
      moveMod.addApplicableUnitType(UnitModel.UnitEnum.MECH);
      myCommander.addCOModifier(moveMod);

      // TODO: Grant transports extra capacity
      // myCommander.getUnitModel(UnitModel.UnitEnum.APC).holdingCapacity += 1;
      // myCommander.getUnitModel(UnitModel.UnitEnum.T_COPTER).holdingCapacity += 1;
      // myCommander.getUnitModel(UnitModel.UnitEnum.LANDER).holdingCapacity += 1;
    }
  }

  /**
   * Mobilize grants Strong a firepower bonus, additional mobility,
   * and the ability to build infantry units from any building he owns.
   */
  private static class MobilizeAbility extends CommanderAbility
  {
    private static final String MOBILIZE_NAME = "Mobilize";
    private static final int MOBILIZE_COST = 0;
    private static final int MOBILIZE_BUFF = 40;

    COModifier damageMod = null;

    MobilizeAbility(Commander commander)
    {
      super(commander, MOBILIZE_NAME, MOBILIZE_COST);

      damageMod = new CODamageModifier(MOBILIZE_BUFF);
    }

    @Override
    protected void perform(GameInstance game)
    {
      myCommander.addCOModifier(damageMod);

      // Make foot-soldiers from all production buildings.
//      myCommander.unitProductionByTerrain.get(TerrainType.CITY).add(myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY));
//      myCommander.unitProductionByTerrain.get(TerrainType.CITY).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
//      myCommander.unitProductionByTerrain.get(TerrainType.AIRPORT).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
//      myCommander.unitProductionByTerrain.get(TerrainType.SEAPORT).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));
//      myCommander.unitProductionByTerrain.get(TerrainType.HEADQUARTERS).add(myCommander.getUnitModel(UnitModel.UnitEnum.MECH));

      // Grant foot-soldiers and transports two (2) additional movement power.
      COMovementModifier moveMod = new COMovementModifier(2);
      moveMod.addApplicableUnitType(UnitModel.UnitEnum.APC);
      moveMod.addApplicableUnitType(UnitModel.UnitEnum.INFANTRY);
      moveMod.addApplicableUnitType(UnitModel.UnitEnum.MECH);
      myCommander.addCOModifier(moveMod);

      // TODO: Grant all transports extra cargo space, and let foot-soldiers hitch a ride on ground vehicles.
      // myCommander.getUnitModel(UnitModel.UnitEnum.APC).holdingCapacity += 1;
      // myCommander.getUnitModel(UnitModel.UnitEnum.T_COPTER).holdingCapacity += 1;
      // myCommander.getUnitModel(UnitModel.UnitEnum.LANDER).holdingCapacity += 1;
      // myCommander.getUnitModel(UnitModel.UnitEnum.RECON).holdingCapacity += 1;
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
