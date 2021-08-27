package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import Engine.XYCoord;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import Engine.Combat.CostValueFinder;
import Engine.Combat.DamagePopup;
import Engine.Combat.MassStrikeUtils;
import Terrain.GameMap;
import Terrain.MapMaster;
import Units.Unit;

public class VB extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
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

  private static class ExMachina extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
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
          findTarget(gameMap),
          0, 2, myCommander, true);
      for( Unit victim : victimList )
      {
        victim.isStunned = true;
      }
    }
    private XYCoord findTarget(GameMap gameMap)
    {
      return MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, false));
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      output.add(new DamagePopup(
                     findTarget(gameMap),
                     myCommander.myColor,
                     "ZAP"));

      return output;
    }
  }
}
