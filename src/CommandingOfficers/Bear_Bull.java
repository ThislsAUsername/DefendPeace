package CommandingOfficers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import CommandingOfficers.Modifiers.COModifier;
import Engine.GameScenario;
import Engine.XYCoord;
import Engine.Combat.DamagePopup;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.GameEvents.ModifyFundsEvent;
import Terrain.GameMap;
import Terrain.MapLocation;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitModel;

/*
 * The Bear & Bull is a dual-CO that sorta models an economy.
 * In Bear mode, he gets less income and also pays less for troops.
 * In Bull mode, he gets more and pays more.
 * His powers let him toggle between these to maximize his gainz.
 */
public class Bear_Bull extends Commander
{
  private static final long serialVersionUID = 1L;
  
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Bear&Bull");
      infoPages.add(new InfoPage(
          "Bear&Bull is a pair of commanders who complement each other like the ebbs and flows of a free market.\n" +
          "They repair units for free, and can leverage their abilities to gain buying power.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "Repairs are always free.\n" +
          "- Bear has 90% prices, but also 90% income.\n" +
          "  - Abilities: Upturn and Boom\n" +
          "- Bull has 120% prices, but also 120% income.\n" +
          "  - Abilities: Downturn and Bust\n"));
      infoPages.add(new InfoPage(
          "Some tips:\n" +
          "Bear wants to build before using Upturn, and spend all cash during Boom\n" +
          "Bull wants to use Downturn before building, and bank cash before using Bust"));
      infoPages.add(new InfoPage(
          "Upturn/Downturn ("+UpDownTurnAbility.DOWNUPTURN_COST+"):\n" +
          "Immediately switches to the other commander, switching back before your next turn starts.\n" +
          "Removes "+UpDownTurnAbility.DOWNUPTURN_LIQUIDATION+" HP from any unit on any property you own.\n" +
          "You get the funds value of all HP removed.\n"));
      infoPages.add(new InfoPage(
          "Boom/Bust ("+BustBoomAbility.BOOMBUST_COST+"):\n" +
          "Grants a 20 percent discount this turn.\n" +
          "Permanently switches to the other commander at the start of your next turn."));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Bear_Bull(rules);
    }
  }

  public boolean isBull;

  private final double BEAR_MOD = 0.9;
  private final double BULL_MOD = 1.2;

  public Bear_Bull(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    // we start in Bear mode, so swap to it at the start
    isBull = true;
    swapD2Ds(true);

    addCommanderAbility(new UpDownTurnAbility(this));
    addCommanderAbility(new BustBoomAbility(this));
  }

  public void swapD2Ds(boolean setIncome)
  {
    if( isBull )
    {
      isBull = false;
      if( setIncome )
        incomeAdjustment = (int) (gameRules.incomePerCity * BEAR_MOD) - gameRules.incomePerCity;
      for( UnitModel um : unitModels )
      {
        um.COcost = BEAR_MOD;
      }
    }
    else
    {
      isBull = true;
      if( setIncome )
        incomeAdjustment = (int) (gameRules.incomePerCity * BULL_MOD) - gameRules.incomePerCity;
      for( UnitModel um : unitModels )
      {
        um.COcost = BULL_MOD;
      }
    }
  }

  @Override
  public double getRepairCostFactor()
  {
    return 0;
  }

  /**
   * Down/UpTurn swaps D2Ds for this turn.
   * All units on a property you own take 3HP mass damage and you gain the funds value of that HP.
   * Bear: Gives you a fast injection of funds based on 1.2x your liquidated units' HP value. Should probably build before using.
   * Bull: Having units on properties when you use it is poor due to the 0.9x liquidation price, however, the discount should be welcome.
   */
  private static class UpDownTurnAbility extends CommanderAbility implements COModifier
  {
    private static final long serialVersionUID = 1L;
    private static final String UPTURN_NAME = "UpTurn";
    private static final String DOWNTURN_NAME = "DownTurn";
    private static final int DOWNUPTURN_COST = 3;
    private static final int DOWNUPTURN_LIQUIDATION = 3;
    Bear_Bull COcast;

    UpDownTurnAbility(Bear_Bull commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, UPTURN_NAME, DOWNUPTURN_COST);
      COcast = commander;
    }

    @Override
    public String toString()
    {
      myName = (COcast.isBull) ? DOWNTURN_NAME : UPTURN_NAME;
      return myName;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(this);
    }

    @Override
    public GameEventQueue getEvents(MapMaster gameMap)
    {
      Collection<Unit> victims = findVictims(gameMap);

      // Damage is dealt after swapping D2Ds so it's actually useful to Bear
      GameEventQueue powerEvents = new GameEventQueue();
      powerEvents.add(new MassDamageEvent(COcast, victims, DOWNUPTURN_LIQUIDATION, false));

      int valueDrained = 0;
      for( Unit victim : victims )
      {
        valueDrained += (Math.min(DOWNUPTURN_LIQUIDATION, victim.getHP()) * victim.model.getCost()) / victim.model.maxHP;
      }

      powerEvents.add( new ModifyFundsEvent(COcast, valueDrained) ); // Collect profits

      return powerEvents;
    }

    @Override // COModifier interface.
    public void applyChanges(Commander commander)
    {
      COcast.swapD2Ds(false);
    }

    @Override
    public void revertChanges(Commander commander)
    {
      COcast.swapD2Ds(true);
    }

    @Override
    public Collection<DamagePopup> getDamagePopups(GameMap gameMap)
    {
      ArrayList<DamagePopup> output = new ArrayList<DamagePopup>();

      for( Unit victim : findVictims(gameMap) )
        output.add(new DamagePopup(
                       new XYCoord(victim.x, victim.y),
                       myCommander.myColor,
                       Math.min(victim.getHP()-1, DOWNUPTURN_LIQUIDATION)*10 + "%"));

      return output;
    }

    public HashSet<Unit> findVictims(GameMap gameMap)
    {
      HashSet<Unit> victims = new HashSet<Unit>(); // Find all of our unlucky participants
      for( XYCoord xyc : myCommander.ownedProperties )
      {
        MapLocation loc = gameMap.getLocation(xyc);
        if( loc.getOwner() == myCommander )
        {
          Unit victim = loc.getResident();
          if( null != victim )
          {
            victims.add(victim);
          }
        }
      }
      return victims;
    }
  }

  /**
   * Boom/Bust reduces costs by 20% (to 70% or 100%). Swap D2Ds permanently when this wears off.
   * In the future, it'd be cool to be able to build on valid locations next to production facilities... but that's hard and I'm lazy
   * For clarification: Boom is Bear's ability; Bust is Bull's
   */
  private static class BustBoomAbility extends CommanderAbility implements COModifier
  {
    private static final long serialVersionUID = 1L;
    private static final String BUST_NAME = "Bust";
    private static final String BOOM_NAME = "Boom";
    private static final int BOOMBUST_COST = 6;
    private static final double BOOMBUST_BUFF = 0.2;
    Bear_Bull COcast;

    BustBoomAbility(Bear_Bull commander)
    {
      // as we start in Bear form, Boom is the correct starting name
      super(commander, BOOM_NAME, BOOMBUST_COST);
      COcast = commander;
    }

    @Override
    public String toString()
    {
      myName = (COcast.isBull) ? BUST_NAME : BOOM_NAME;
      return myName;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      myCommander.addCOModifier(this);
    }

    @Override // COModifier interface.
    public void applyChanges(Commander commander)
    {
      // Instead of swapping, we get a discount. Yaaaay.
      for( UnitModel um : COcast.unitModels )
      {
        um.COcost -= BOOMBUST_BUFF;
      }
    }

    @Override // COModifier interface.
    public void revertChanges(Commander commander)
    {
      // Next turn, we swap D2Ds permanently
      COcast.swapD2Ds(true);
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
