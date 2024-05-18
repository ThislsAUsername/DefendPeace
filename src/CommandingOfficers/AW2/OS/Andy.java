package CommandingOfficers.AW2.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AW2.AW2Commander;
import Engine.GameScenario;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;

public class Andy extends AW2Commander
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
      super("Andy", UIUtils.SourceGames.AW2, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Andy (AW2)\n"
          + "A mechanical boy wonder. Impulsive to a fault, he’s fiercely protective of friends.\n"
          + "No real weaknesses. Proficient with air, sea, and land units. Ready to fight wherever and whenever.\n"));
      infoPages.add(new InfoPage(new HyperRepair(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Restores 2 HP to all units.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new HyperUpgrade(null, new CostBasis(CHARGERATIO_FUNDS)),
            "Restores "+HyperUpgrade.HEAL+" HP to all units.\n"
          + "Firepower rises (+"+HyperUpgrade.BUFF+"), and unit movement increases by 1 space.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Mechanics\n"
          + "Miss: Waking up early"));
      infoPages.add(AW2_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Andy(rules);
    }
  }

  public Andy(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
    addCommanderAbility(new HyperRepair(this, cb));
    addCommanderAbility(new HyperUpgrade(this, cb));
  }

  private static class HyperRepair extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Hyper Repair";
    private static final int COST = 3;
    private static final int HEAL = 2;

    HyperRepair(Andy commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      AIFlags = 0; // Why one would ever use this is beyond me
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit u : myCommander.army.getUnits() )
      {
        u.alterHealth(HEAL*10);
      }
      super.perform(gameMap);
    }
  }

  private static class HyperUpgrade extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Hyper Upgrade";
    private static final int COST = 6;
    private static final int BUFF = 20;
    private static final int HEAL = 5;
    UnitModifier statMod;
    UnitModifier moveMod;

    HyperUpgrade(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      statMod = new UnitDamageModifier(BUFF);
      moveMod = new UnitMovementModifier(1);
      AIFlags = PHASE_TURN_START;
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(statMod);
      modList.add(moveMod);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      for( Unit u : myCommander.army.getUnits() )
      {
        u.alterHealth(HEAL*10);
      }
      super.perform(gameMap);
    }
  }

}
