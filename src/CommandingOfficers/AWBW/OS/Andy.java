package CommandingOfficers.AWBW.OS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.CommanderAbility.CostBasis;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;

public class Andy extends AWBWCommander
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
      super("Andy_BW", UIUtils.SourceGames.AWBW, UIUtils.OS);
      infoPages.add(new InfoPage(
            "Andy (AWBW)\n"
          + "No day-to-day abilities.\n"));
      infoPages.add(new InfoPage(new HyperRepair(null, new CostBasis(CHARGERATIO_FUNDS)),
            "All units gain +2 HP.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new HyperUpgrade(null, new CostBasis(CHARGERATIO_FUNDS)),
            "All units gain +"+HyperUpgrade.HEAL+"HP, +"+HyperUpgrade.BUFF+"% attack, and +1 movement.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
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

  private static class HyperRepair extends AWBWAbility
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
        u.alterHealthNoRound(HEAL*10);
      }
      super.perform(gameMap);
    }
  }

  private static class HyperUpgrade extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Hyper Upgrade";
    private static final int COST = 6;
    private static final int BUFF = 10;
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
        u.alterHealthNoRound(HEAL*10);
      }
      super.perform(gameMap);
    }
  }

}
