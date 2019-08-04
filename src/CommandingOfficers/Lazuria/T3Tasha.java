package CommandingOfficers.Lazuria;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.IDS.TabithaEngine;
import CommandingOfficers.Modifiers.COMovementModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class T3Tasha extends TabithaEngine
{
  public static final int MEGA_ATK = 40;
  public static final int MEGA_DEF = 20;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("T3Tasha");
      infoPages.add(new InfoPage(
            "Tasha, but worse.\n"
          + "Can grant a single boost of +"+MEGA_ATK+"/"+MEGA_DEF+" stats, but only to air units; this power-up lasts until next turn.\n"
          + "xxXXX\n"
          + "Sonic Boom: All air units gain +1 movement.\n"
          + "Night Vision: All air units gain +1 movement and +"+MEGA_ATK+"/"+MEGA_DEF+" stats."));
      infoPages.add(MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new T3Tasha(rules);
    }
  }

  @Override
  public int getMegaBoostCount() {return 1;};
  @Override
  public boolean canBoost(UnitModel type)
  {return type.chassis == ChassisEnum.AIR_HIGH || type.chassis == ChassisEnum.AIR_LOW;}

  public T3Tasha(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);

    addCommanderAbility(new AirMoveBonus(this, "Sonic Boom", 1, 1));
    addCommanderAbility(new FoxOne(this));
  }

  private static class AirMoveBonus extends CommanderAbility
  {
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
      for( UnitModel um : myCommander.unitModels.values() )
      {
        if( um.chassis == ChassisEnum.AIR_HIGH ||  um.chassis == ChassisEnum.AIR_LOW)
        {
          airMoveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(airMoveMod);
    }
  }

  protected static class FoxOne extends nonStackingBoost
  {
    private static final String NAME = "Fox One";
    static final int COST = 5;

    protected FoxOne(TabithaEngine commander)
    {
      super(commander, NAME, COST, MEGA_ATK, MEGA_DEF);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      COMovementModifier airMoveMod = new COMovementModifier(2);
      for( UnitModel um : myCommander.unitModels.values() )
      {
        if( um.chassis == ChassisEnum.AIR_HIGH ||  um.chassis == ChassisEnum.AIR_LOW)
        {
          airMoveMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(airMoveMod);
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
