package CommandingOfficers;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import AI.AICombatUtils;
import AI.AIController;
import AI.AILibrary;
import AI.AIMaker;
import CommandingOfficers.Modifiers.COModifier;
import Engine.GameAction;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.BattleSummary;
import Engine.Combat.CombatContext;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.UuidGenerator;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.MapMaster;
import Terrain.MapWindow;
import Terrain.TerrainType;
import UI.GameOverlay;
import UI.UIUtils.Faction;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModelScheme.GameReadyModels;

public class Commander implements GameEventListener, Serializable
{
  private static final long serialVersionUID = 1L;
  
  public final CommanderInfo coInfo;
  public final GameScenario.GameRules gameRules;
  public MapWindow myView;
  public ArrayList<Unit> units;
  public ArrayList<UnitModel> unitModels = new ArrayList<UnitModel>();
  public Map<TerrainType, ArrayList<UnitModel>> unitProductionByTerrain;
  public Set<XYCoord> ownedProperties;
  public ArrayList<COModifier> modifiers;
  public Color myColor;
  public Faction faction;
  public static final int CHARGERATIO_FUNDS = 9000; // quantity of funds damage to equal 1 unit of power charge
  public static final int CHARGERATIO_HP = 100; // Funds value of 1 HP damage dealt, for the purpose of power charge
  public int money = 0;
  public int incomeAdjustment = 0; // Commander subclasses can increase/decrease income if needed.
  public int team = -1;
  public boolean isDefeated = false;
  public XYCoord HQLocation = null;
  private double myAbilityPower = 0;

  private ArrayList<CommanderAbility> myAbilities = null;
  private String myActiveAbilityName = "";

  // The AI has to be effectively stateless anyway (to be able to adapt to whatever scenario it finds itself in on map start),
  //   so may as well not require them to care about serializing their contents.
  private transient AIController aiController = null;

  private long passSalt;
  private UUID password;

  public Commander(CommanderInfo info, GameScenario.GameRules rules)
  {
    coInfo = info;
    gameRules = rules;

    // Fetch our fieldable unit types from the rules
    GameReadyModels GRMs = rules.unitModelScheme.getGameReadyModels();
    unitProductionByTerrain = GRMs.shoppingList;
    for (UnitModel um : GRMs.unitModels)
      unitModels.add(um);

    modifiers = new ArrayList<COModifier>();
    units = new ArrayList<Unit>();
    ownedProperties = new HashSet<XYCoord>();

    myAbilities = new ArrayList<CommanderAbility>();
  }

  protected void addCommanderAbility(CommanderAbility ca)
  {
    myAbilities.add(ca);
  }

  /**
   * These functions allow a Commander to inject modifications before evaluating a battle.
   * Simple damage buffs, etc. can be accomplished via COModifiers, but effects
   * that depend on circumstances that must be evaluated at combat time (e.g. a
   * terrain-based firepower bonus) can be handled here.
   * The following three functions will serve for most combat changes, like the above example.
   * changeCombatContext() allows the CO to make more drastic changes like counterattacking first or at 2+ range.
   */
  public void changeCombatContext(CombatContext instance)
  {}
  /**
   * Called any time you are making a weapon attack.
   * Applies to all potential targets, whether they be units or not.
   * Should be used to modify attacks from your units
   *   any time you do not need specific information about the target.
   */
  public void modifyUnitAttack(StrikeParams params)
  {}
  /**
   * Called any time you are attacking a unit, always after {@link #modifyUnitAttack(StrikeParams)}
   * Applies only when attacking a unit.
   * Should be used only when you need specific information about your target.
   */
  public void modifyUnitAttackOnUnit(BattleParams params)
  {}
  /**
   * Called any time your unit is being attacked, after {@link #modifyUnitAttackOnUnit(BattleParams)}
   * Should be used to modify attacks made against your units.
   */
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {}

  public void addCOModifier(COModifier mod)
  {
    mod.applyChanges(this);
    modifiers.add(mod); // Add to the list so the modifier can be reverted next turn.
  }

  public void endTurn()
  {
    if( aiController != null ) aiController.endTurn();
    myView.resetFog();
  }

  /**
   * Collect income and handle any COModifiers.
   * @param map
   */
  public GameEventQueue initTurn(MapMaster map)
  {
    myView.resetFog();
    myActiveAbilityName = "";

    // Accrue income for each city under your control.
    money += getIncomePerTurn();

    // Un-apply any modifiers that were activated last turn.
    // TODO: If/when we have modifiers that last multiple turns, figure out how to handle them.
    for( int i = modifiers.size() - 1; i >= 0; --i )
    {
      modifiers.get(i).revertChanges(this);
      modifiers.remove(i);
    }

    if( null != aiController )
    {
      aiController.initTurn(myView);
    }

    GameEventQueue events = new GameEventQueue();
    for( Unit u : units )
    {
      events.addAll(u.initTurn(map));
    }

    return events;
  }

  /**
   * @return whether these COs would like to kill each other
   */
  public boolean isEnemy(Commander other)
  {
    // If the other CO doesn't exist, we can't be friends.
    if( other == null )
      return true;
    // If the other CO is us, we can't *not* be friends.
    if( other == this )
      return false;
    // If we have no team, we have no friends.
    if( team < 0 || other.team < 0 )
      return true;
    // If we have teams and we're on the same team...
    // we're on the same team.
    if( team == other.team )
      return false;
    // Otherwise, we hate each other.
    return true;
  }

  public UnitModel getUnitModel(long unitRole)
  {
    return getUnitModel(unitRole, true);
  }
  public UnitModel getUnitModel(long unitRole, boolean matchOnAny)
  {
    UnitModel um = null;

    for( UnitModel iter : unitModels )
    {
      boolean some = iter.isAny(unitRole);
      boolean all = iter.isAll(unitRole);
      if( all || (some && matchOnAny) )
      {
        um = iter;
        break;
      }
    }

    return um;
  }

  public ArrayList<UnitModel> getAllModels(long unitRole)
  {
    return getAllModels(unitRole, true);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny)
  {
    return getAllModels(unitRole, matchOnAny, 0);
  }
  public ArrayList<UnitModel> getAllModelsNot(long excludedUnitRoles)
  {
    long allFlags = ~0;
    return getAllModels(allFlags, true, excludedUnitRoles);
  }
  public ArrayList<UnitModel> getAllModels(long unitRole, boolean matchOnAny, long excludedRoles)
  {
    ArrayList<UnitModel> models = new ArrayList<UnitModel>();

    for( UnitModel iter : unitModels )
    {
      boolean some = iter.isAny(unitRole) && iter.isNone(excludedRoles);
      boolean all = iter.isAll(unitRole) && iter.isNone(excludedRoles);
      if( all || (some && matchOnAny) )
      {
        models.add(iter);
      }
    }

    return models;
  }
  
  /**
   * Returns a character to be displayed on the unit.
   * Primary usage should be pieces of info that aren't otherwise immediately apparent from the map.
   * Our rendering only supports alphanumeric values at this time.
   */
  public char getUnitMarking(Unit unit)
  {
    // We don't have anything useful to print, so don't.
    return '\0';
  }

  /** Get the list of units this commander can build from the given property type. */
  public ArrayList<UnitModel> getShoppingList(Location buyLocation)
  {
    return (unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType) != null) ? unitProductionByTerrain.get(buyLocation.getEnvironment().terrainType)
        : new ArrayList<UnitModel>();
  }

  /** Return an ArrayList containing every ability this Commander currently has the power to perform. */
  public ArrayList<CommanderAbility> getReadyAbilities()
  {
    ArrayList<CommanderAbility> ready = new ArrayList<CommanderAbility>();
    if( myActiveAbilityName.isEmpty() )
    {
      for( CommanderAbility ca : myAbilities )
      {
        if( ca.getCost() <= myAbilityPower )
        {
          ready.add(ca);
        }
      }
    }
    return ready;
  }

  public double[] getAbilityCosts()
  {
    double[] costs = new double[myAbilities.size()];
    for( int i = 0; i < myAbilities.size(); ++i )
    {
      costs[i] = myAbilities.get(i).getCost();
    }
    return costs;
  }

  public double getAbilityPower()
  {
    return myAbilityPower;
  }

  public String getActiveAbilityName()
  {
    return myActiveAbilityName;
  }

  /** Lets the commander know that he's using an ability,
   *  and accounts for the cost of using it. */
  public void activateAbility(CommanderAbility ability)
  {
    modifyAbilityPower(-ability.getCost());
    myActiveAbilityName = ability.toString();
  }

  public void modifyAbilityPower(double amount)
  {
    myAbilityPower += amount;
    if( myAbilityPower < 0 )
      myAbilityPower = 0;
    double maxPower = getMaxAbilityPower();
    if( myAbilityPower > maxPower )
      myAbilityPower = maxPower;
  }

  /**
   * @return The maximum level of ability energy this CO can hold
   */
  public double getMaxAbilityPower()
  {
    double maxPower = 0;
    for( CommanderAbility ca : myAbilities )
    {
      if( maxPower < ca.getCost() )
      {
        maxPower = ca.getCost();
      }
    }
    return maxPower;
  }

  // TODO: determine if this needs parameters, and if so, what?
  public double getRepairCostFactor()
  {
    return 1;
  }

  // TODO: determine if this needs parameters, and if so, what?
  public int getRepairPower()
  {
    return 2;
  }

  public ArrayList<Unit> threatsToOverlay = new ArrayList<Unit>();
  public ArrayList<GameOverlay> getMyOverlays(GameMap gameMap, boolean amIViewing)
  {
    ArrayList<GameOverlay> overlays = new ArrayList<GameOverlay>();
    // Apply any relevant map highlight.
    if( !amIViewing )
      return overlays;
    for( Unit u : threatsToOverlay )
    {
      XYCoord uCoord = new XYCoord(u.x, u.y);
      if( !gameMap.isLocationValid(uCoord) )
        continue;

      int r = u.CO.myColor.getRed(), g = u.CO.myColor.getGreen(), b = u.CO.myColor.getBlue();
      Color edgeColor = new Color(r, g, b, 200);
      Color fillColor = new Color(r, g, b, 100);
      overlays.add(new GameOverlay(uCoord,
                   AICombatUtils.findThreatPower(gameMap, u, null).keySet(),
                   fillColor, edgeColor));
    }
    return overlays;
  }
  /**
   * Track unit deaths, so I know not to be threatened by them.
   */
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer hpBeforeDeath)
  {
    threatsToOverlay.remove(victim);
    return null;
  }

  /**
   * Track battles that happen, and get ability power based on combat this CO is in.
   */
  @Override
  public GameEventQueue receiveBattleEvent(final BattleSummary summary)
  {
    // We only care who the units belong to, not who picked the fight. 
    Unit minion = null;
    double myHPLoss = -10;
    Unit enemy = null;
    double myHPDealt = -10;
    if( this == summary.attacker.CO )
    {
      minion = summary.attacker;
      myHPLoss = summary.attackerHPLoss;
      enemy = summary.defender;
      myHPDealt = summary.defenderHPLoss;
    }
    if( this == summary.defender.CO )
    {
      minion = summary.defender;
      myHPLoss = summary.defenderHPLoss;
      enemy = summary.attacker;
      myHPDealt = summary.attackerHPLoss;
    }

    // Do nothing if we're not involved
    if( minion != null && enemy != null )
    {
      double power = 0; // value in funds of the charge we're getting

      // Add up the funds value of the damage done to both participants.
      power += myHPLoss / minion.model.maxHP * minion.model.getCost();
      // The damage we deal is worth half as much as the damage we take, to help powers be a comeback mechanic.
      power += myHPDealt / enemy.model.maxHP * enemy.model.getCost() / 2;
      // Add power based on HP damage dealt; rewards aggressiveness.
      power += myHPDealt * CHARGERATIO_HP;
      
      // Convert funds to ability power units
      power /= CHARGERATIO_FUNDS;

      modifyAbilityPower(42);
//      modifyAbilityPower(power);
    }
    return null;
  }

  /**
   * Track mass damage done to my units, and get ability power based on it.
   */
  @Override
  public GameEventQueue receiveMassDamageEvent(Commander attacker, Map<Unit, Integer> lostHP)
  {
    if( this == attacker )
      return null; // Punching yourself shouldn't make you angry

    for( Entry<Unit, Integer> damageEntry : lostHP.entrySet() )
    {
      Unit unit = damageEntry.getKey();
      if (this == unit.CO)
      {
        double power = 0; // value in funds of the charge we're getting

        power += ((double)damageEntry.getValue()) / unit.model.maxHP * unit.model.getCost();

        // Convert funds to ability power units
        power /= CHARGERATIO_FUNDS;
        modifyAbilityPower(power);
      }
    }
    return null;
  }

  public void setAIController(AIController ai)
  {
    aiController = ai;
  }

  public String getControllerName()
  {
    if( null != aiController )
      return aiController.getAIInfo().getName();
    return "Human";
  }

  public boolean isAI()
  {
    return (aiController != null);
  }

  public GameAction getNextAIAction(GameMap gameMap)
  {
    if( aiController != null )
    {
      return aiController.getNextAction(myView);
    }
    return null;
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void writeObject(ObjectOutputStream stream) throws IOException
  {
    stream.defaultWriteObject();

    // save our index into the AILibrary
    // TODO: Consider serializing AI as well, so we don't need this method
    if( null == aiController )
      stream.writeInt(0); // Humans live at index 0 of the AI array. That sounds philosophical.
    else
    {
      for( AIMaker AI : AILibrary.getAIList() )
      {
        if( AI.getName().equalsIgnoreCase(aiController.getAIInfo().getName()) )
          stream.writeInt(AILibrary.getAIList().indexOf(AI));
      }
    }
  }

  /**
   * Private method, same signature as in Serializable interface
   *
   * @param stream
   * @throws IOException
   */
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
  {
    stream.defaultReadObject();

    // use our AI index to get back where we were before
    aiController = AILibrary.getAIList().get(stream.readInt()).create(this);
  }

  /** Return true if this Commander is password-protected, false else. */
  public boolean hasPassword()
  {
    return password != null;
  }

  /** Salt the provided string with the Commander's salt and return the result. */
  private UUID hashPass(String pass)
  {
    int numBytes = Long.BYTES + (Character.BYTES*pass.length());
    ByteBuffer bb = ByteBuffer.allocate(numBytes);
    bb.putLong(passSalt).put(pass.getBytes());
    UUID hashedPass = UuidGenerator.sha1Uuid(pass);
    return hashedPass;
  }

  /** Assigns the given salt and password to this Commander, salting
   * and hashing the password before storing.
   * Throws an exception if a password has already been set. */
  public void setPassword(long salt, String pass)
  {
    if( hasPassword() )
      throw new UnsupportedOperationException("Cannot set new password! Password is already set!");

    passSalt = salt;
    password = hashPass(pass);
  }

  /** Return true if the provided password is correct, false if not. */
  public boolean checkPassword(String pass)
  {
    if( !hasPassword() )
      throw new UnsupportedOperationException("Cannot check password! No password is set!");

    return password.equals(hashPass(pass));
  }

  /**
   * Count up the number of profitable properties we own, multiply by the game's income-per-city
   * setting, and tack on any CO-specific income modifier, then return the result.
   */
  public int getIncomePerTurn()
  {
    int count = 0;
    for( XYCoord coord : ownedProperties )
    {
      // Re-check ownership just because.
      if( myView.getLocation(coord).getOwner() == this && myView.getLocation(coord).isProfitable() ) ++count;
    }
    return count * (gameRules.incomePerCity + incomeAdjustment);
  }
}
