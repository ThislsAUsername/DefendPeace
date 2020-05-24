package CommandingOfficers.IDS;

import Engine.GameScenario;
import java.util.ArrayList;

import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.CommanderInfo.InfoPage;
import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier.GenericUnitModifier;
import Engine.Combat.BattleSummary;
import Engine.Combat.CostValueFinder;
import Engine.Combat.MassStrikeUtils;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameAction;
import Engine.GameActionSet;
import Engine.Path;
import Engine.UnitActionFactory;
import Engine.XYCoord;
import Terrain.GameMap;
import Terrain.MapMaster;
import UI.MapView;
import UI.Art.Animation.GameAnimation;
import Units.Unit;
import Units.UnitModel;

public abstract class TabithaEngine extends Commander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage MECHANICS_BLURB = new InfoPage(
            "Mega Boost mechanics:\n"
          + "A Mega Boost is awarded when a unit attacks, or by a special action done in place (does not end turn).\n"
          + "Mega Boosted units gain the generic +10/10 on powers, but no power-specific stat boost\n");
  public ArrayList<Unit> COUs = new ArrayList<Unit>();
  public abstract int getMegaBoostCount();
  public void onCOULost(Unit minion) {};
  public boolean canBoost(UnitModel type) {return true;};
  public final int COUPow; // static values that define the power the COU should stay at
  public final int COUDef;
  private int megaPow; // floating values that dip on powers to match the above
  private int megaDef;
  private boolean canApplyBoost = false;

  public TabithaEngine(int atk, int def, CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    COUPow = atk;
    COUDef = def;
    megaPow = COUPow;
    megaDef = COUDef;

    for( UnitModel um : unitModels )
    {
      if( canBoost(um) )
        um.possibleActions.add(new MegaBoost(this));
    }
  }

  @Override
  public char getUnitMarking(Unit unit)
  {
    if( COUs.contains(unit) )
      return 'M';

    return super.getUnitMarking(unit);
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.COUs.clear();
    megaPow = COUPow;
    megaDef = COUDef;
    canApplyBoost = true;
    return super.initTurn(map);
  }

  @Override
  public void endTurn()
  {
    canApplyBoost = false;
    super.endTurn();
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    boolean freeBoost = canApplyBoost && COUs.size() < getMegaBoostCount();
    if( (freeBoost && canBoost(params.attacker.body.model)) || COUs.contains(params.attacker.body) )
      params.attackPower += megaPow;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    boolean freeBoost = canApplyBoost && COUs.size() < getMegaBoostCount();
    if( (freeBoost && canBoost(params.defender.body.model)) || COUs.contains(params.defender.body) )
      params.defensePower += megaDef;
  }

  @Override
  public void receiveBattleEvent(BattleSummary battleInfo)
  {
    super.receiveBattleEvent(battleInfo);
    boolean freeBoost = canApplyBoost && COUs.size() < getMegaBoostCount();
    // Determine if we were part of this fight.
    if( (freeBoost && canBoost(battleInfo.attacker.model)) && battleInfo.attacker.CO == this )
    {
      COUs.add(battleInfo.attacker);
    }


    Unit minion = null;
    if( this == battleInfo.defender.CO )
    {
      minion = battleInfo.defender;
    }
    if( this == battleInfo.attacker.CO )
    {
      minion = battleInfo.attacker;
    }
    if (null != minion && minion.getHP() < 1 && COUs.contains(minion))
    {
      onCOULost(minion);
    }
  }

  protected static class nonStackingBoost extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    TabithaEngine COcast;
    private int atk, def;

    protected nonStackingBoost(TabithaEngine commander, String name, int cost, int pAtk, int pDef)
    {
      super(commander, name, cost);
      COcast = commander;
      atk = pAtk;
      def = pDef;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.megaPow -= atk;
      COcast.megaDef -= def;
      GenericUnitModifier powMod = new CODamageModifier(atk);
      GenericUnitModifier defMod = new CODefenseModifier(def);
      for( UnitModel um : myCommander.unitModels )
      {
        if( COcast.canBoost(um) )
        {
          powMod.addApplicableUnitModel(um);
          defMod.addApplicableUnitModel(um);
        }
      }
      COcast.addCOModifier(powMod);
      COcast.addCOModifier(defMod);
    }
  }

  protected static class NukeIt extends nonStackingBoost
  {
    private static final long serialVersionUID = 1L;
    private int nukePower;

    protected NukeIt(TabithaEngine commander, String name, int cost, int nuke, int pAtk, int pDef)
    {
      super(commander, name, cost, pAtk, pDef);
      nukePower = nuke;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      XYCoord target = MassStrikeUtils.findValueConcentration(gameMap, 2, new CostValueFinder(myCommander, true));
      MassStrikeUtils.damageStrike(gameMap, nukePower, target, 2);
    }
  }

  //////////////////////////////////////////////////////////
  // Mega action jazz happens after this point
  //////////////////////////////////////////////////////////

  private static class MegaBoost extends UnitActionFactory
  {
    private static final long serialVersionUID = 1L;
    final TabithaEngine tabby;
    public MegaBoost(TabithaEngine owner)
    {
      tabby = owner;
    }

    @Override
    public GameActionSet getPossibleActions(GameMap map, Path movePath, Unit actor, boolean ignoreResident)
    {
      XYCoord moveLocation = new XYCoord(movePath.getEnd().x, movePath.getEnd().y);
      if( moveLocation.equals(actor.x, actor.y) && tabby.COUs.size() < tabby.getMegaBoostCount() )
      {
        return new GameActionSet(new ApplyMegaBoost(this, actor), false);
      }
      return null;
    }

    @Override
    public String name()
    {
      return "MEGA BOOST";
    }
  }

  private static class ApplyMegaBoost implements GameAction
  {
    final MegaBoost type;
    final Unit actor;
    final XYCoord destination;
    public ApplyMegaBoost(MegaBoost owner, Unit unit)
    {
      type = owner;
      actor = unit;
      destination = new XYCoord(unit.x, unit.y);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      GameEventQueue eventSequence = new GameEventQueue();
      eventSequence.add(new MegaBoostEvent(type.tabby, actor));
      return eventSequence;
    }

    @Override
    public String toString()
    {
      return String.format("[Mega Boost %s in place]", actor.toStringWithLocation());
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

  private static class MegaBoostEvent implements GameEvent
  {
    final TabithaEngine tabby;
    private Unit unit;

    public MegaBoostEvent(TabithaEngine owner, Unit unit)
    {
      tabby = owner;
      this.unit = unit;
    }

    @Override
    public GameAnimation getEventAnimation(MapView mapView)
    {
      return null;
    }

    @Override
    public void sendToListener(GameEventListener listener)
    {
    }

    @Override
    public void performEvent(MapMaster gameMap)
    {
      tabby.COUs.add(unit);
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
