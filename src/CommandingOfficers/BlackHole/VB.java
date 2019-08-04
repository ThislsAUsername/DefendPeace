package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Terrain.MapMaster;
import Units.Unit;

public class VB extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Von Bolt");
      infoPages.add(new InfoPage(
          "Von Bolt\r\n" + 
          "  Units gain +10% attack and +10% defense\r\n" + 
          "Ex Machina -- A 2 Range missile hits the accumulation of the opponent's most expensive units, does 3 HP damage to them and stuns them during their next turn"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new VB(rules);
    }
  }

  public VB(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    new CODamageModifier(10).applyChanges(this);
    new CODefenseModifier(10).applyChanges(this);

    addCommanderAbility(new ExMachina(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public char getUnitMarking(Unit unit)
  {
    if (unit.isStunned)
      return 'S';
    
    return super.getUnitMarking(unit);
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

