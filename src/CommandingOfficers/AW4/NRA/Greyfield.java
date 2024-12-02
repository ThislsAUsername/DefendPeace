package CommandingOfficers.AW4.NRA;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.DeployableCommander;
import CommandingOfficers.AW4.RuinedCommander;
import Engine.GameScenario;
import Terrain.MapMaster;
import UI.UIUtils;
import Units.Unit;
import Units.UnitModel;

public class Greyfield extends RuinedCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Greyfield", UIUtils.SourceGames.AW4, UIUtils.NRA);
      infoPages.add(new InfoPage(
          "Ambitious and power hungry, he wants to destroy the Lazurian Army and rule the world as its new king.\n"));
      infoPages.add(new InfoPage(
          "Base Zone: "+RADIUS+"\n"
          + "Zone Boost: Sea/copters/seaplanes +"+POWER+"/"+DEFENSE+".\n"));
      infoPages.add(new InfoPage(new SupplyChain(null),
          "Refills fuel, ammo and materials for all units.\n"));
      infoPages.add(DeployableCommander.COU_MECHANICS_BLURB);
      infoPages.add(RuinedCommander.DOR_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Greyfield(rules);
    }
  }

  public static final int RADIUS  = 3;
  public static final int POWER   = 10;
  public static final int DEFENSE = 40;

  public Greyfield(GameScenario.GameRules rules)
  {
    super(RADIUS, POWER, DEFENSE, coInfo, rules);
    this.boostMaskAny = UnitModel.SEA | UnitModel.HOVER; // Also Seaplanes, via override

    addCommanderAbility(new SupplyChain(this));
  }

  @Override
  public boolean shouldBoost(UnitModel model)
  {
    return "seaplane".equalsIgnoreCase(model.name) || super.shouldBoost(model);
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  protected static class SupplyChain extends RuinedAbility
  {
    private static final long serialVersionUID = 1L;

    protected SupplyChain(RuinedCommander commander)
    {
      super(commander, "Supply Chain");
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      for( Unit unit : myCommander.army.getUnits() )
      {
        unit.resupply();
        unit.materials = unit.model.maxMaterials;
      }
    }
  }

}
