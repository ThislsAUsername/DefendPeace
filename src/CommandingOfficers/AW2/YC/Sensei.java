package CommandingOfficers.AW2.YC;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AW2.AW2Commander;
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

public class Sensei extends AW2Commander
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
      super("Sensei", UIUtils.SourceGames.AW2, UIUtils.YC);
      infoPages.add(new InfoPage(
            "Sensei (AW2)\n"
          + "A former paratrooper rumoured to have been quite the CO in his day.\n"
          + "Powerful infantry & high transport movement range. Superior firepower for copters, but weak vs. naval and vehicle units.\n"
          + "(+50/0 copters/footsoldiers, -10/0 non-air units)\n"));
      infoPages.add(new InfoPage(new CopterCommand(null, null),
            "Attack copter firepower increases (+25). Infantry units with 9 HP appear in all allied cities, ready to be moved.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(new AirborneAssault(null, null),
            "Attack copter firepower increases (+25). Mech units with 9 HP appear in all allied cities, ready to be moved.\n"
          + "+10 defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Lazy, rainy days\n"
          + "Miss: Busy malls"));
      infoPages.add(AW2_MECHANICS_BLURB);
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

    CommanderAbility.CostBasis cb = new CommanderAbility.CostBasis(CHARGERATIO_FUNDS);
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

  private static class SenseiPower extends AW2Ability
  {
    private static final long serialVersionUID = 1L;
    UnitTypeFilter copterMod;
    UnitModel deployable;

    SenseiPower(Sensei commander, String name, int cost, CostBasis basis, UnitModel deployable)
    {
      super(commander, name, cost, basis);
      copterMod = new UnitTypeFilter(new UnitDamageModifier(25));
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
            events.add(new CreateUnitEvent(myCommander, deployable, new XYCoord(x, y), AnimationStyle.DROP_IN, unitIsReady, allowStomping));
            //TODO: 9 HP
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
