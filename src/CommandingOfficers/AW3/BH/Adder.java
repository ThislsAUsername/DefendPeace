package CommandingOfficers.AW3.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;

public class Adder extends AW3Commander
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
      super("Adder", UIUtils.SourceGames.AW3, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Adder (AW3)\n"
          + "A self-absorbed CO who believes his skills are matchless. Second to Hawke in rank.\n"
          + "Adept at making quick command decisions, he stores up energy for his CO Power more rapidly than other COs.\n"
          + "(Uh, sort of)"));
      infoPages.add(new InfoPage(new Sideslip(null, null),
            "Movement range of all units is increased by one space.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new Sidewinder(null, null),
            "Movement range of all units is increased by two spaces.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: His Own Face\n"
          + "Miss: Dirty Things"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

  private static class Sideslip extends AW3Ability
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

  private static class Sidewinder extends AW3Ability
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
