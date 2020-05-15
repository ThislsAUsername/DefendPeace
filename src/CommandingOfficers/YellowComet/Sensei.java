package CommandingOfficers.YellowComet;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.XYCoord;
import Engine.GameEvents.CreateUnitEvent;
import Terrain.MapMaster;
import Terrain.Location;
import Terrain.TerrainType;
import Units.UnitModel;

public class Sensei extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Sensei");
      infoPages.add(new InfoPage(
          "Sensei\r\n" + 
          "  Copters gain +50% attack, footsoldiers gain +40% attack, but all other non-air units lose -10% attack. Transports gain +1 movement\r\n" + 
          "Copter Command -- Copters' strength is increased by +15%; 9 HP Infantry units are placed on every owned, empty city\r\n" + 
          "Airborne Assault -- Copters' strength is increased by +15%; 9 HP Mech units are placed on every owned, empty city"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sensei(rules);
    }
  }

  public Sensei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    COMovementModifier moveMod = new COMovementModifier();

    for( UnitModel um : unitModels )
    {
      if (um.isAny(UnitModel.AIR_LOW))
        um.modifyDamageRatio(50);
      if (um.isAny(UnitModel.TROOP))
        um.modifyDamageRatio(40);
      if (um.isNone(UnitModel.AIR_LOW
                   | UnitModel.AIR_HIGH
                   | UnitModel.TROOP))
          um.modifyDamageRatio(-10);

      if (um.isAny(UnitModel.TRANSPORT))
        moveMod.addApplicableUnitModel(um);
    }

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
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Copter Command";
    private static final int COST = 2;

    CopterCommand(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      UnitModel spawn = myCommander.getUnitModel(UnitModel.TROOP);
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
      copterPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.AIR_LOW | UnitModel.ASSAULT));
      myCommander.addCOModifier(copterPowerMod);
    }
  }

  private static class AirborneAssault extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Airborne Assault";
    private static final int COST = 6;

    AirborneAssault(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      UnitModel spawn = myCommander.getUnitModel(UnitModel.MECH);
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
      copterPowerMod.addApplicableUnitModel(myCommander.getUnitModel(UnitModel.AIR_LOW | UnitModel.ASSAULT));
      myCommander.addCOModifier(copterPowerMod);
    }
  }
}

