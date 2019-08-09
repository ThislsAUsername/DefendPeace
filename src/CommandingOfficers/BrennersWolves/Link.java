package CommandingOfficers.BrennersWolves;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.IDS.TabithaEngine;
import CommandingOfficers.Modifiers.COVisionModifier;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Terrain.MapMaster;
import Units.UnitModel;
import Units.UnitModel.ChassisEnum;

public class Link extends TabithaEngine
{
  private static final long serialVersionUID = 1L;
  public static final int MEGA_ATK = 20;
  public static final int MEGA_DEF = 20;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Link");
      infoPages.add(new InfoPage(
            "No relation to 'Lin', whoever that is.\n"
          + "Can grant a single boost of +"+MEGA_ATK+"/"+MEGA_DEF+" stats, but only to ground units; this power-up lasts until next turn.\n"
          + "xxXXXX\n"
          + "Scout: All ground units gain +1 vision and can see into hiding places.\n"
          + "Night Vision: All ground units gain +"+MEGA_ATK+"/"+MEGA_DEF+" stats, +2 vision, and can see into hiding places."));
      infoPages.add(MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Link(rules);
    }
  }

  @Override
  public int getMegaBoostCount() {return 1;};
  @Override
  public boolean canBoost(UnitModel type)
  {return type.chassis == ChassisEnum.TANK || type.chassis == ChassisEnum.TROOP;}

  public Link(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);

    addCommanderAbility(new Scout(this));
    addCommanderAbility(new NightVision(this));
  }

  private static class Scout extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Scout";
    private static final int COST = 2;

    Scout(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // add vision +1 and piercing vision to land units
      COVisionModifier sightMod = new COVisionModifier(1);
      for( UnitModel um : myCommander.unitModels.values() )
      {
        if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
          sightMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  protected static class NightVision extends nonStackingBoost
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Night Vision";
    static final int COST = 6;

    protected NightVision(TabithaEngine commander)
    {
      super(commander, NAME, COST, MEGA_ATK, MEGA_DEF);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      // add vision +2 and piercing vision to land units
      GenericUnitModifier sightMod = new COVisionModifier(2);
      for( UnitModel um : myCommander.unitModels.values() )
      {
        if( um.chassis == ChassisEnum.TANK || um.chassis == ChassisEnum.TROOP )
        {
          sightMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
