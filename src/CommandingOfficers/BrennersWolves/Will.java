package CommandingOfficers.BrennersWolves;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class Will extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Will");
      infoPages.add(new InfoPage(
          "--WILL--\r\n" +
          "All ground direct units (including foot soldiers) gain +20% firepower.\r\n" +
          "xxXXX\r\n" +
          "RALLY CRY: All ground direct units gain +1 movement\r\n" +
          "A NEW ERA: All ground direct units gain +2 movement"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Will(rules);
    }
  }

  public Will(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isLandUnit() && um.hasMobileWeapon() )
      {
        um.modifyDamageRatio(20);
      }
    }

    addCommanderAbility(new GoFast(this, "Rally Cry", 2, 1));
    addCommanderAbility(new GoFast(this, "A New Era", 5, 2));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class GoFast extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private final int power;

    GoFast(Commander commander, String name, int cost, int oomph)
    {
      super(commander, name, cost);
      power = oomph;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COMovementModifier moveMod = new COMovementModifier(power);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isLandUnit() && um.hasMobileWeapon() )
        {
          moveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(moveMod);
    }
  }
}
