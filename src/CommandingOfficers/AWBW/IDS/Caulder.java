package CommandingOfficers.AWBW.IDS;

import java.util.ArrayList;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.Army;
import Engine.GameScenario;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassHealEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Units.Unit;
import lombok.var;

public class Caulder extends AWBWCommander
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
      super("Caulder", UIUtils.SourceGames.AWBW, UIUtils.IDS);
      infoPages.add(new InfoPage(
            "Caulder (AWBW)\n"
          + "Repair your units by 2 HP.\n"));
      infoPages.add(new InfoPage(new SupremeBoost(null, null),
            "+50/35 (160/145) stats, and repair another 3 HP.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Caulder(rules);
    }
  }

  public Caulder(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new SupremeBoost(this, cb));
  }

  public static final int D2DREPAIRS = 2;
  @Override
  protected void onTurnInit(MapMaster map, GameEventQueue events)
  {
    queueGlobalRepairs(this.army, map, events, D2DREPAIRS);
  }


  private static class SupremeBoost extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Supreme Boost";
    private static final int COST = 10;
    UnitModifier atkMod, defMod;
    
    SupremeBoost(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      atkMod = new UnitDamageModifier(50);
      defMod = new UnitDefenseModifier(35);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(atkMod);
      modList.add(defMod);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      GameEventQueue events = new GameEventQueue();
      queueGlobalRepairs(myCommander.army, map, events, 3);
      return events;
    }
  }

  public static void queueGlobalRepairs(Army army, MapMaster map, GameEventQueue events, int repairHP)
  {
    final int repairs = repairHP*10;
    var patients = new ArrayList<Unit>();

    // Bounding box to limit wasted iterations
    final int minX = 0;
    final int minY = 0;
    final int maxX = map.mapWidth  - 1;
    final int maxY = map.mapHeight - 1;

    for( int y = minY; y <= maxY; y++ ) // Top to bottom, left to right
    {
      for( int x = minX; x <= maxX; x++ )
      {
        Unit resi = map.getResident(x, y);
        if( resi != null && army == resi.CO.army )
          patients.add(resi);
      }
    }

    if( patients.isEmpty() )
      return;
    var heal = new MassHealEvent(army, patients, repairs); // Event handles cost logic
    events.add(heal);
  }

}
