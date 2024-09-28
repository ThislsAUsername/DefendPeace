package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Adder extends AWBWCommander
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
      super("Adder", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Adder (AWBW)\n"
          + "No day-to-day abilities."));
      infoPages.add(new InfoPage(new Sideslip(null, null),
            "All units gain +1 movement.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new Sidewinder(null, null),
            "All units gain +2 movement.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Adder(rules);
    }
  }

  public Adder(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new Sideslip(this, cb));
    addCommanderAbility(new Sidewinder(this, cb));
  }

  private static class Sideslip extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Sideslip";
    private static final int COST = 2;
    UnitModifier moveMod;

    Sideslip(Adder commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitMovementModifier(1);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

  private static class Sidewinder extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Sidewinder";
    private static final int COST = 5;
    UnitModifier moveMod;

    Sidewinder(Adder commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitMovementModifier(2);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
    }
  }

}
