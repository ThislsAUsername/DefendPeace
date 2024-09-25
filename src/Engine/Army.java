package Engine;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;

import AI.AIController;
import AI.AILibrary;
import AI.AIMaker;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import Engine.Combat.BattleSummary;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitActionLifecycles.JoinLifecycle.JoinEvent;
import Engine.UnitMods.UnitModList;
import Engine.UnitMods.UnitModifier;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Terrain.MapPerspective;
import UI.GameOverlay;
import UI.UnitMarker;
import Units.Unit;
import Units.UnitDelta;
import Units.UnitModel;

public class Army implements GameEventListener, Serializable, UnitModList, UnitMarker
{
  private static final long serialVersionUID = 1L;

  public MapPerspective myView;
  public final GameScenario.GameRules gameRules;
  public Commander[] cos;
  public int money = 0;
  public int team = -1;
  public boolean isDefeated = false;
  public ArrayList<XYCoord> HQLocations = new ArrayList<>();

  // The AI has to be effectively stateless anyway (to be able to adapt to whatever scenario it finds itself in on map start),
  //   so may as well not require them to care about serializing their contents.
  private transient AIController aiController = null;

  private long passSalt;
  private UUID password;

  public Army(GameScenario scenario)
  {
    gameRules = scenario.rules;
  }
  public Army(GameScenario scenario, Commander co)
  {
    this(scenario);
    cos = new Commander[] { co };
    co.army = this;
  }

  public void initForGame(GameInstance game)
  {
    this.registerForEvents(game);
    for( Commander co : cos )
      co.initForGame(game);
  }
  public void deInitForGame(GameInstance game)
  {
    this.unregister(game);
    for( Commander co : cos )
      co.deInitForGame(game);
  }

  public GameEventQueue getAbilityRevertEvents(MapMaster map)
  {
    GameEventQueue events = new GameEventQueue();

    for( Commander co : cos )
    {
      events.addAll(co.getAbilityRevertEvents(map));
    }

    return events;
  }

  @Override
  public GameEventQueue receiveTurnInitEvent(MapMaster map, Army army, int turn)
  {
    if( this == army && null != aiController )
      aiController.initTurn(myView);

    return null;
  }

  @Override
  public GameEventQueue receiveTurnEndEvent(Army army, int turn)
  {
    if( this == army && aiController != null )
      aiController.endTurn();

    return null;
  }

  /**
   * @return whether these armies would like to kill each other
   */
  public boolean isEnemy(Army other)
  {
    // If the other army doesn't exist, we can't be friends.
    if( other == null )
      return true;
    // If the other army is us, we can't *not* be friends.
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
  public boolean isEnemy(Commander co)
  {
    if( co == null )
      return true;
    return isEnemy(co.army);
  }

  public ArrayList<XYCoord> getOwnedProperties()
  {
    ArrayList<XYCoord> output = new ArrayList<>();
      for( Commander co : cos )
        output.addAll(co.ownedProperties);
    return output;
  }
  public int getIncomePerTurn()
  {
    int total = 0;
      for( Commander co : cos )
        total += co.getIncomePerTurn();
    return total;
  }
  public ArrayList<Unit> getUnits()
  {
    ArrayList<Unit> output = new ArrayList<>();
      for( Commander co : cos )
        output.addAll(co.units);
    return output;
  }
  public boolean canBuildUnits()
  {
    int armyCount = 0;
    for( Commander co : cos )
      armyCount += co.units.size();
    return gameRules.unitCap > armyCount;
  }
  public boolean canBuyOn(MapLocation loc)
  {
    // TODO: maybe calculate whether we have enough money to buy something at this industry
    final Unit resident = loc.getResident();
    final Commander co = loc.getOwner();
    if( null == resident && null != co && co.army == this )
    {
      if( co.getShoppingList(loc).size() > 0 )
      {
        return true;
      }
    }
    return false;
  }
  public int getBuyCost(UnitModel um, XYCoord coordinates)
  {
    MapLocation loc = myView.getLocation(coordinates);
    return loc.getOwner().getBuyCost(um, coordinates);
  }

  /** Return a list with every ability a Commander in this Army can perform. */
  public ArrayList<CommanderAbility> getReadyAbilities()
  {
    // If there's a primary Commander, only that one can use abilities
    if( gameRules.tagMode.supportsMultiCmdrSelect )
      return cos[0].getReadyAbilities();

    // Otherwise, be laissez-faire
    ArrayList<CommanderAbility> ready = new ArrayList<CommanderAbility>();
    for( Commander co : cos )
      ready.addAll(co.getReadyAbilities());
    return ready;
  }
  /**
   * Concatenates all active ability names and returns the result
   */
  public String getAbilityText()
  {
    ArrayList<String> activeNames = new ArrayList<>();
    for( Commander co : cos )
      if( co.getActiveAbility() != null )
        activeNames.add(co.getActiveAbility().toString());

    // If no abilities are active, that's it
    if( activeNames.isEmpty() )
      return "";

    String output = "";
    output += activeNames.get(0);
    // Start at 1 so we don't double-add the first one
    for( int i = 1; i < activeNames.size(); ++i )
      output += " + " + activeNames.get(i);

    return output;
  }

  public ArrayList<Unit> threatsToOverlay = new ArrayList<Unit>();
  /**
   * Calculates any relevant map highlight
   * @param amIViewing Whether to include information that's for my own Army's eyes only
   */
  public ArrayList<GameOverlay> getMyOverlays(MapPerspective gameMap, boolean amIViewing)
  {
    ArrayList<GameOverlay> overlays = new ArrayList<GameOverlay>();
    for( Commander co : cos )
      overlays.addAll(co.getMyOverlays(gameMap, amIViewing));

    return overlays;
  }
  /**
   * Track unit deaths, so I know not to be threatened by them.
   */
  @Override
  public GameEventQueue receiveUnitDieEvent(Unit victim, XYCoord grave, Integer healthBeforeDeath)
  {
    threatsToOverlay.remove(victim);
    return null;
  }
  @Override
  public GameEventQueue receiveUnitJoinEvent(JoinEvent join)
  {
    threatsToOverlay.remove(join.unitDonor);
    return null;
  }
  // Draw markings on units we're threat-overlaying
  @Override
  public char getUnitMarking(Unit unit, Army activeArmy)
  {
    if( this != activeArmy || !threatsToOverlay.contains(unit) )
      return '\0';

    return 'T';
  }
  @Override
  public Color getMarkingColor(Unit unit)
  {
    if( this == unit.CO.army )
      return Color.GREEN;
    return Color.RED;
  }

  public void awbwTagsCharge(Function<Commander, Integer> chargeEvaluator)
  {
    if( !cos[0].canAcceptCharge() )
      return;
    final int primaryCharge = chargeEvaluator.apply(cos[0]);
    cos[0].modifyAbilityPower(primaryCharge);
    final int tagDivisor = 2;
    for( int i = 1; i < cos.length; ++i )
    {
      final int tagCharge = chargeEvaluator.apply(cos[i]);
      cos[i].modifyAbilityPower(tagCharge / tagDivisor);
    }
  }
  public void persistentTagsCharge(Function<Commander, Integer> chargeEvaluator)
  {
    if( !cos[0].canAcceptCharge() )
      return;
    // As persistent tags is meant to be mostly a sidegrade, give half charge to the primary, and split the rest among the rest.
    final int primaryCharge = chargeEvaluator.apply(cos[0]);
    if( cos.length == 1 )
      cos[0].modifyAbilityPower(primaryCharge);
    else
    {
      cos[0].modifyAbilityPower(primaryCharge / 2);
      final double tagMultiplier = 0.5 / (cos.length - 1);
      for( int i = 1; i < cos.length; ++i )
      {
        final int tagCharge = chargeEvaluator.apply(cos[i]);
        cos[i].modifyAbilityPower((int) (tagCharge * tagMultiplier));
      }
    }
  }

  /**
   * Track battles that happen, and get ability power based on combat this CO is in.
   */
  @Override
  public GameEventQueue receiveBattleEvent(final BattleSummary summary)
  {
    // The lambdas demand the args be effectively final, so we're duplicating checks today
    boolean amAttacking = this == summary.attacker.CO.army;
    if( !amAttacking && this != summary.defender.CO.army )
      return null; // not attacking and not the defender -> not involved

    // We only care who the units belong to, not who picked the fight.
    final UnitDelta minion;
    final UnitDelta enemy;
    final boolean isCounter = !amAttacking;
    if( amAttacking )
    {
      minion = summary.attacker;
      enemy = summary.defender;
    }
    else
    {
      minion = summary.defender;
      enemy = summary.attacker;
    }

    Function<Commander, Integer> chargeEvaluator = (co) -> co.calculateCombatCharge(minion, enemy, isCounter);
    switch (gameRules.tagMode)
    {
      case AWBW:
        awbwTagsCharge(chargeEvaluator);
        break;
      case Persistent:
        persistentTagsCharge(chargeEvaluator);
        break;
      case Team_Merge:
      case OFF:
        if( minion.CO.canAcceptCharge() )
        {
          final int ownerCharge = chargeEvaluator.apply(minion.CO);
          minion.CO.modifyAbilityPower(ownerCharge);
        }
        break;
    }
    return null;
  }

  /**
   * Track mass damage done to my units, and get ability power based on it.
   */
  @Override
  public GameEventQueue receiveMassDamageEvent(Commander attacker, Map<Unit, Integer> lostHealth)
  {
    if( attacker != null && this == attacker.army )
      return null; // Punching yourself shouldn't make you angry

    for( Entry<Unit, Integer> damageEntry : lostHealth.entrySet() )
    {
      Unit minion = damageEntry.getKey();
      if( this != minion.CO.army )
        break;
      Function<Commander, Integer> chargeEvaluator = (co) -> co.calculateMassDamageCharge(minion, damageEntry.getValue());
      switch (gameRules.tagMode)
      {
        case AWBW:
          awbwTagsCharge(chargeEvaluator);
          break;
        case Persistent:
          persistentTagsCharge(chargeEvaluator);
          break;
        case Team_Merge:
        case OFF:
          if( minion.CO.canAcceptCharge() )
          {
            final int ownerCharge = chargeEvaluator.apply(minion.CO);
            minion.CO.modifyAbilityPower(ownerCharge);
          }
          break;
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

  private final ArrayList<UnitModifier> unitMods = new ArrayList<UnitModifier>();;
  @Override
  public List<UnitModifier> getModifiers()
  {
    return new ArrayList<UnitModifier>(unitMods);
  }

  @Override
  public void addUnitModifier(UnitModifier unitModifier)
  {
    unitMods.add(unitModifier);
  }
  @Override
  public void removeUnitModifier(UnitModifier unitModifier)
  {
    unitMods.remove(unitModifier);
  }
}
