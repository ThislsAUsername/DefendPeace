package CommandingOfficers.OrangeStar;

import Engine.GameScenario;
import Engine.XYCoord;

import java.util.ArrayList;
import java.util.Collection;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import Engine.Combat.CaptureUnitValueFinder;
import Engine.Combat.CostValueFinder;
import Engine.Combat.DamagePopup;
import Engine.Combat.HPValueFinder;
import Engine.Combat.MassStrikeUtils;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.GameMap;
import Terrain.MapMaster;

public class Rachel extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Rachel");
      infoPages.add(new InfoPage(
          "Rachel\r\n" + 
          "  +1 HP for repairs (liable for costs)\r\n" + 
          "Lucky Lass -- Improves Luck (0-40%)\r\n" + 
          "Covering Fire -- Three 2-range missiles strike the opponents' greatest accumulation of footsoldier HP, unit funds value, and unit HP (in that order)."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Rachel(rules);
    }
  }

  private int luckMax = 10;

  public Rachel(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new LuckyLass(this));
    addCommanderAbility(new CoveringFire(this));
  }

  @Override
  public int getRepairPower()
  {
    return 3;
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.luckMax = 10;
    return super.initTurn(map);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    params.luckMax = luckMax;
  }

  private static class LuckyLass extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Lucky Lass";
    private static final int COST = 3;
    private static final int VALUE = 40;
    Rachel COcast;

    LuckyLass(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (Rachel) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.luckMax = VALUE;
    }
  }

  private static class CoveringFire extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Covering Fire";
    private static final int COST = 6;

    CoveringFire(Commander commander)
    {
      super(commander, NAME, COST);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // inf, cost, HP in order
      // deets: https://discordapp.com/channels/313453805150928906/314370192098459649/392908214913597442
      XYCoord[] targets = findTargets(gameMap);
      MassStrikeUtils.missileStrike(gameMap, targets[0]);
      MassStrikeUtils.missileStrike(gameMap, targets[1]);
      MassStrikeUtils.missileStrike(gameMap, targets[2]);
    }
    private XYCoord[] findTargets(GameMap gameMap)
    {
      XYCoord[] targets = new XYCoord[3];
      targets[0] = MassStrikeUtils.findValueConcentration(gameMap, 2, new CaptureUnitValueFinder(myCommander,false));
      targets[1] = MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander,true));
      targets[2] = MassStrikeUtils.findValueConcentration(gameMap, 2, new HPValueFinder(myCommander,true));
      return targets;
    }
    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      XYCoord[] targets = findTargets(gameMap);
      addPopup(output, targets[0], "INF");
      addPopup(output, targets[1], "COST");
      addPopup(output, targets[2], "HP");

      return output;
    }

    private void addPopup(ArrayList<DamagePopup> popups, XYCoord target, String callout)
    {
      for( DamagePopup popup : popups )
      {
        if( popup.coords.equals(target) )
        {
          popup.quantity += ", " + callout;
          return;
        }
      }
      popups.add(new DamagePopup(target, myCommander.myColor, callout));
    }
  }
}

