package CommandingOfficers;

import Engine.GameScenario;

import java.util.ArrayList;
import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitDefenseModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitTypeFilter;
import Engine.Army;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.GamePath;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.TerrainType;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitContext;
import Units.UnitModel;

public abstract class DeployableCommander extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage ZONE_MECHANICS_BLURB = new InfoPage(
            "CO Unit mechanics:\n"
          + "Deploy your CO Unit via a special action done in place (does not end turn).\n"
          + "You can't deploy a CO Unit that has already been active during your turn (i.e. must wait a turn after deleting it/dying to a counter).\n");
  public ArrayList<Unit> COUs = new ArrayList<Unit>();
  public ArrayList<Unit> COUsLost = new ArrayList<Unit>();
  /** The number of COUs you can have active at once */
  public abstract int getCOUCount();
  public void onCOULost(Unit minion) {};
  public char getCOUMark() {return 'C';};
  /** If the unit type matches any flag in this mask, it can be my COU */
  public long canDeployMask = Long.MAX_VALUE;
  public final boolean canDeployOn(UnitModel type) {return type.isAny(canDeployMask);};
  public final int COUPow; // static values that define the power the COU should stay at
  public final int COUDef;
  private int megaPow; // floating values that dip on powers to match the above
  private int megaDef;

  public boolean flexibleBoost = true;

  /** Can I deploy my COU here? */
  protected boolean eligibleDeployLocation(Unit actor, MapLocation loc)
  {
    return getShoppingList(loc).contains(actor.model)
        || loc.getEnvironment().terrainType == TerrainType.HEADQUARTERS
        || loc.getEnvironment().terrainType == TerrainType.LAB;
  }


  public DeployableCommander(int atk, int def, CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    COUPow = atk;
    COUDef = def;
    megaPow = COUPow;
    megaDef = COUDef;
  }

  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    if( COUs.contains(unit) )
      return getCOUMark();

    return super.getUnitMarking(unit, activeArmy);
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    if( flexibleBoost )
    {
      this.COUs.clear();
    }
    else
    {
      this.COUs.removeAll(COUsLost);
      this.COUsLost.clear();
    }
    megaPow = COUPow;
    megaDef = COUDef;
    return super.initTurn(map);
  }

  @Override
  public void endTurn()
  {
    super.endTurn();
  }

  @Override
  public void modifyActionList(UnitContext uc)
  {
    if( canDeployOn(uc.model) )
      uc.actionTypes.add(new DeployCOU(this));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( COUs.contains(params.attacker.unit) )
      params.attackPower += megaPow;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( COUs.contains(params.defender.unit) )
      params.defensePower += megaDef;
  }

  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
  {
    // COUs die when they are killed
    if( COUs.contains(victim) )
    {
      COUsLost.add(victim);
      onCOULost(victim);
    }
    return super.receiveUnitDieEvent(victim, grave, hpBeforeDeath);
  }
  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    // COUs shouldn't vanish into the aether on joining another unit
    if( COUs.contains(join.unitDonor) )
    {
      COUs.remove(join.unitDonor);
      COUs.add(join.unitRecipient);
    }
    return super.receiveUnitJoinEvent(join);
  }

  protected static class nonStackingBoost extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    DeployableCommander COcast;
    private int atk, def;
    UnitTypeFilter powMod;
    UnitTypeFilter defMod;

    protected nonStackingBoost(DeployableCommander commander, String name, int cost, int pAtk, int pDef)
    {
      super(commander, name, cost);
      COcast = commander;
      atk = pAtk;
      def = pDef;
      powMod = new UnitTypeFilter(new UnitDamageModifier(atk));
      defMod = new UnitTypeFilter(new UnitDefenseModifier(def));
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.megaPow -= atk;
      COcast.megaDef -= def;
      powMod.allOf = COcast.canDeployMask;
      defMod.allOf = COcast.canDeployMask;
    }

    @Override
    protected void enqueueUnitMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(powMod);
      modList.add(defMod);
    }
  }

  //////////////////////////////////////////////////////////
  // Action definition happens after this point
  //////////////////////////////////////////////////////////

  private static class DeployCOU extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    final DeployableCommander deployer;
    public DeployCOU(DeployableCommander owner)
    {
      deployer = owner;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, GamePath movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( moveLocation.equals(actor.x, actor.y)
          && deployer.COUs.size() < deployer.getCOUCount()
          && (deployer.flexibleBoost || deployer.eligibleDeployLocation(actor, map.getLocation(moveLocation))) )
      {
        return new GameActionSet(new ApplyMegaBoost(this, actor), false);
      }
      return null;
    }

    @Override
    public String name(Unit actor)
    {
      return "Deploy COU";
    }
  }

  private static class ApplyMegaBoost extends GameAction
  {
    final DeployCOU type;
    final Unit actor;
    final XYCoord destination;
    public ApplyMegaBoost(DeployCOU owner, Unit unit)
    {
      type = owner;
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      eventSequence.add(new DeployCOUEvent(type.deployer, actor));
      return eventSequence;
    }

    @Override
    public String toString()
    {
      return String.format("[Deploy COU on %s in place]", actor.toStringWithLocation());
    }

    @Override
    public UnitActionFactory getType()
    {
      return type;
    }

    @Override
    public XYCoord getMoveLocation()
    {
      return destination;
    }

    @Override
    public XYCoord getTargetLocation()
    {
      return destination;
    }
  } // ~ApplyMegaBoost

  private static class DeployCOUEvent implements GameEvent
  {
    final DeployableCommander deployer;
    private Unit unit;

    public DeployCOUEvent(DeployableCommander owner, Unit unit)
    {
      deployer = owner;
      this.unit = unit;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public GameEventQueue sendToListener(GameEventListener listener)
    {
      return null;
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      deployer.COUs.add(unit);
    }

    @Override
    public XYCoord getStartPoint()
    {
      return new XYCoord(unit.x, unit.y);
    }

    @Override
    public XYCoord getEndPoint()
    {
      return new XYCoord(unit.x, unit.y);
    }
  }
}
