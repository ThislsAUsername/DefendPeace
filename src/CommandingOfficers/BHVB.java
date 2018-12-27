package CommandingOfficers;

import java.util.ArrayList;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Terrain.GameMap;
import Units.Unit;

public class BHVB extends Commander
{
  private static final CommanderInfo coInfo = new CommanderInfo("Von Bolt", new instantiator());

  private static class instantiator implements COMaker
  {
    @Override
    public Commander create()
    {
      return new BHVB();
    }
  }

  public BHVB()
  {
    super(coInfo);

    new CODamageModifier(10).apply(this);
    new CODefenseModifier(10).apply(this);

    addCommanderAbility(new ExMachina(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  private static class ExMachina extends CommanderAbility
  {
    private static final String NAME = "Ex Machina";
    private static final int COST = 10;
    private static final int POWER = 3;

    ExMachina(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(GameMap gameMap)
    {
      ArrayList<Unit> victimList = MassStrikeUtils.damageStrike(gameMap, POWER,
          MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true)), 2);
      for( Unit victim : victimList )
      {
        victim.isStunned = true;
      }
    }
  }
}
