package CommandingOfficers.YellowComet;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.XYCoord;
import Engine.GameEvents.CreateUnitEvent;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Units.UnitModel;

public class Hetler extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Hetler");
      infoPages.add(new InfoPage(
          "Hetler (rebalanced Sensei)\r\n" + 
          "  Copters gain +50% attack, footsoldiers gain +40% attack, but all other non-air units lose -10% attack. Transports gain +1 movement\r\n" + 
          "Copter Command (3): Copters' strength is increased by +15%\r\n" + 
          "Airborne Assault (9): Copters' strength is increased by +15%; 9 HP Infantry units are placed on every owned, empty city"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Hetler(rules);
    }
  }

  public Hetler(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels.values() )
    {
      switch (um.chassis)
      {
        case AIR_LOW:
          um.modifyDamageRatio(50);
          break;
        case TROOP:
          um.modifyDamageRatio(40);
          break;
        case AIR_HIGH:
          break;
        default:
          um.modifyDamageRatio(-10);
          break;
      }
    }

    COMovementModifier moveMod = new COMovementModifier();
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.APC));
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.T_COPTER));
    moveMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.LANDER));
    moveMod.applyChanges(this);

    addCommanderAbility(new CopterCommand(this));
    addCommanderAbility(new AirborneAssault(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class CopterCommand extends CommanderAbility
  {
    private static final String NAME = "Airborne Assault";
    private static final int COST = 3;

    CopterCommand(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      CODamageModifier copterPowerMod = new CODamageModifier(15);
      copterPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.B_COPTER));
      myCommander.addCOModifier(copterPowerMod);
    }
  }

  private static class AirborneAssault extends CommanderAbility
  {
    private static final String NAME = "Airborne Assault";
    private static final int COST = 9;

    AirborneAssault(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      UnitModel spawn = myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY);
      for( XYCoord xyc : myCommander.ownedProperties )
      {
        Location loc = gameMap.getLocation(xyc);
        if( loc.getEnvironment().terrainType == TerrainType.CITY && loc.getResident() == null)
        {
          CreateUnitEvent cue = new CreateUnitEvent(myCommander,spawn,loc.getCoordinates());
          myCommander.money += spawn.getCost();
          cue.performEvent(gameMap);
          loc.getResident().alterHP(-1);
          loc.getResident().isTurnOver = false;
        }
      }
      CODamageModifier copterPowerMod = new CODamageModifier(15);
      copterPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.UnitEnum.B_COPTER));
      myCommander.addCOModifier(copterPowerMod);
    }
  }
}

