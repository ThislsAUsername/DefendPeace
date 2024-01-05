package CommandingOfficers.AW1.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.UnitMods.VisionModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Units.UnitContext;
import Terrain.MapMaster;

public class Sonja extends AW1Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Sonja_1", UIUtils.SourceGames.AW1, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Sonja (AW1)\n"
          + "Kanbei's daughter. An intel gathering genius.\n"
          + "Units have great range of vision. Is plagued by constant bad luck.\n"
          + "(+1 vision, -15 to 9 single-roll luck)\n"));
      infoPages.add(new InfoPage(new EnhancedVision(null),
            "Extends vision range (total +3) of all units. Shows enemy units hidden in woods, reefs and other areas.\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: Computers\n"
          + "Miss: Bugs"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sonja(rules);
    }
  }

  public Sonja(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new EnhancedVision(this));
  }
  @Override
  public void modifyVision(UnitContext uc)
  {
    uc.visionRange += 1;
  }

  private static class EnhancedVision extends AW1Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Enhanced Vision";
    private static final int COST = 6;
    UnitModifier sightMod;

    EnhancedVision(Sonja commander)
    {
      super(commander, NAME, COST);
      sightMod = new VisionModifier(2);
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      super.enqueueUnitMods(gameMap, modList);
      modList.add(sightMod);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      myCommander.army.myView.revealFog();
    }
  }

}
