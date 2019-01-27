package CommandingOfficers;

import java.util.ArrayList;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Terrain.MapMaster;
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

  @Override
  public char getSymbol(Unit unit)
  {
    // If the unit belongs to someone else, let the owner print something.
    if (this != unit.CO && '\0' != unit.CO.getSymbol(unit))
      return unit.CO.getSymbol(unit);
    
    if (unit.isStunned)
      return 'S';
    
    return '\0';
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
    protected void perform(MapMaster gameMap)
    {
      ArrayList<Unit> victimList = MassStrikeUtils.damageStrike(gameMap, POWER,
          MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, false)),
          0, 2, myCommander, true);
      for( Unit victim : victimList )
      {
        victim.isStunned = true;
      }
    }
  }
}
