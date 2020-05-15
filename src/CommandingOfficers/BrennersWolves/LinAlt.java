package CommandingOfficers.BrennersWolves;

import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import CommandingOfficers.Modifiers.COVisionModifier;
import Terrain.MapMaster;
import Units.UnitModel;

public class LinAlt extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Lin Alt");
      infoPages.add(new InfoPage(
          "--LIN Alt--\r\n" + 
          "Ground units gain +5/5 stats\r\n" + 
          "xxXXX\r\n" + 
          "SCOUT: All ground units get +1 vision, and can see into hiding places.\r\n" + 
          "NIGHT VISION: All ground units get +15/10 stats, +2 vision, and can see into hiding places."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new LinAlt(rules);
    }
  }

  public LinAlt(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    for( UnitModel um : unitModels )
    {
      if( um.isLandUnit() )
      {
        um.modifyDamageRatio(5);
        um.modifyDefenseRatio(5);
      }
    }

    addCommanderAbility(new Scout(this));
    addCommanderAbility(new NightVision(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
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
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isLandUnit() )
          sightMod.addApplicableUnitModel(um);
      }
      myCommander.addCOModifier(sightMod);
      myCommander.myView.revealFog();
    }
  }

  private static class NightVision extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Night Vision";
    private static final int COST = 5;

    NightVision(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // add vision +2 and piercing vision to land units
      GenericUnitModifier sightMod = new COVisionModifier(2);
      GenericUnitModifier powMod = new CODamageModifier(15);
      GenericUnitModifier defMod = new CODefenseModifier(10);
      for( UnitModel um : myCommander.unitModels )
      {
        if( um.isLandUnit() )
        {
          sightMod.addApplicableUnitModel(um);
          powMod.addApplicableUnitModel(um);
          defMod.addApplicableUnitModel(um);
        }
      }
      myCommander.addCOModifier(sightMod);
      myCommander.addCOModifier(powMod);
      myCommander.addCOModifier(defMod);
      myCommander.myView.revealFog();
    }
  }
}

