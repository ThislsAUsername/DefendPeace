package CommandingOfficers.AWBW.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.CreateUnitEvent;
import Engine.GameEvents.CreateUnitEvent.AnimationStyle;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;
import lombok.var;

public class Sensei extends AWBWCommander
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
      super("Sensei", UIUtils.SourceGames.AWBW, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Sensei (AWBW)\n"
          + "Copters gain +50% attack, footsoldiers gain +40% attack, but all other non-air units lose -10% attack. Transports gain +1 movement.\n"));
      infoPages.add(new InfoPage(new CopterCommand(null, null),
            "Copters' attack is increased to +65%. 9 HP unwaited infantry are placed on every owned, empty city.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new AirborneAssault(null, null),
            "Copters' attack is increased to +65%. 9 HP unwaited mechs are placed on every owned, empty city.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Sensei(rules);
    }
  }

  public Sensei(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new CopterCommand(this, cb));
    addCommanderAbility(new AirborneAssault(this, cb));
  }
  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(UnitModel.HOVER | UnitModel.TROOP) )
      params.attackPower += 50;
    if( params.attacker.model.isNone(UnitModel.AIR_HIGH | UnitModel.AIR_LOW) )
      params.attackPower -= 10;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.baseCargoCapacity > 0 && uc.model.weapons.isEmpty() )
      uc.movePower += 1;
  }

  private static class SenseiPower extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter copterMod;
    UnitModel deployable;

    SenseiPower(Sensei commander, String name, int cost, CostBasis basis, UnitModel deployable)
    {
      super(commander, name, cost, basis);
      copterMod = new UnitTypeFilter(new UnitDamageModifier(15));
      copterMod.oneOf = UnitModel.HOVER;
      this.deployable = deployable;
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }
    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(copterMod);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      var events = new GameEventQueue();
      boolean allowStomping = false;
      boolean unitIsReady = true;

      // Bounding box to limit wasted iterations
      final int minX = 0;
      final int minY = 0;
      final int maxX = map.mapWidth-1;
      final int maxY = map.mapHeight-1;

      for( int y = minY; y <= maxY; y++ ) // Top to bottom, left to right
      {
        for( int x = minX; x <= maxX; x++ )
        {
          var loc = map.getLocation(x, y);
          if( loc.getEnvironment().terrainType != TerrainType.CITY )
            continue;
          Unit resi = loc.getResident();
          var owner = loc.getOwner();
          if( resi == null && null != owner && myCommander.army == owner.army )
          {
            CreateUnitEvent dudeSpawn = new CreateUnitEvent(myCommander, deployable, new XYCoord(x, y), AnimationStyle.NONE, unitIsReady, allowStomping);
            dudeSpawn.myNewUnit.health = 90;
            events.add(dudeSpawn);
          }
        }
      }
      return events;
    }
  }

  private static class CopterCommand extends SenseiPower
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Copter Command";
    private static final int COST = 2;

    CopterCommand(Sensei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis, (null == commander) ? null : commander.getUnitModel(UnitModel.TROOP));
    }
  }
  private static class AirborneAssault extends SenseiPower
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Airborne Assault";
    private static final int COST = 6;

    AirborneAssault(Sensei commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis, (null == commander) ? null : commander.getUnitModel(UnitModel.MECH));
    }
  }
}
