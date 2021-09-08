package CommandingOfficers.Lazuria;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class Tasha extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Tasha");
      infoPages.add(new InfoPage(
          "--TASHA--\r\n" +
          "Air units gain +"+D2D_ATTACK+"% firepower and +"+D2D_DEFENSE+"% defense.\r\n" +
          "SONIC BOOM ("+COP_COST+"): All air units gain +"+COP_MOVE+" movement.\r\n" +
          "FOX ONE ("+SCOP_COST+"): All air units gain +"+SCOP_MOVE+" movement."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Tasha(rules);
    }
  }
  public static final int D2D_ATTACK = 40;
  public static final int D2D_DEFENSE = 20;
  public static final int COP_COST = 2;
  public static final int COP_MOVE = 2;
  public static final int SCOP_COST = 5;
  public static final int SCOP_MOVE = 4;

  public Tasha(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isAirUnit() )
      {
        um.modifyDamageRatio(D2D_ATTACK);
        um.modifyDefenseRatio(D2D_DEFENSE);
      }
    }

    addCommanderAbility(new AirMoveBonus(this, "Sonic Boom", COP_COST, COP_MOVE));
    addCommanderAbility(new AirMoveBonus(this, "Fox One", SCOP_COST, SCOP_MOVE));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class AirMoveBonus extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private int power = 1;

    AirMoveBonus(Commander commander, String name, int cost, int buff)
    {
      super(commander, name, cost);
      power = buff;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier airMoveMod = new COMovementModifier(power);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isAirUnit() )
        {
          airMoveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(airMoveMod);
    }
  }
}

