package CommandingOfficers;
import CommandingOfficers.Modifiers.COModifier;
import Engine.XYCoord;
import Terrain.Location;
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
  private static final long serialVersionUID = -3048055535977630509L;
  
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Bear&Bull");
      infoPages.add(new InfoPage(
          "Bear&Bull is a pair of commanders who complement each other like the ebbs and flows of a free market.\n" +
          "They have no constant advantage, but can leverage their abilities to gain buying power.\n"));
      infoPages.add(new InfoPage(
          "Passive:\n" +
          "- Bear pays and gets 90% cash.\n" +
          "  - Abilities: Upturn and Boom\n" +
          "- Bull pays and gets 120% cash.\n" +
          "  - Abilities: Downturn and Bust\n"));
      infoPages.add(new InfoPage(
          "Upturn/Downturn ("+UpDownTurnAbility.DOWNUPTURN_COST+"):\n" +
          "Switches temporarily to the other commander. Does not affect income.\n" +
          "Removes "+UpDownTurnAbility.DOWNUPTURN_LIQUIDATION+" HP from any unit on any property you own.\n" +
          "You get the funds value of all HP removed.\n"));
      infoPages.add(new InfoPage(
          "Boom/Bust ("+BustBoomAbility.BOOMBUST_COST+"):\n" +
          "Permanently switches in the other commander.\n" +
          "Also grants a 20 percent discount this turn.\n"));
    }
    @Override
    public Commander create()
    {
      return new Bear_Bull();
    }
  }
  
  private final int incomeBase;
  public boolean isBull;

  private final double BEAR_MOD = 0.9;
  private final double BULL_MOD = 1.2;

  public Bear_Bull()
  {
    super(coInfo);

    incomeBase = incomePerCity;
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
        incomePerCity = (int) (incomeBase * BEAR_MOD);
      for( UnitModel um : unitModels.values() )
      {
        um.COcost = BEAR_MOD;
      }
    }
    else
    {
      isBull = true;
      if( setIncome )
        incomePerCity = (int) (incomeBase * BULL_MOD);
      for( UnitModel um : unitModels.values() )
      {
        um.COcost = BULL_MOD;
      }
    }
  }

  /**
   * Down/UpTurn swaps D2Ds for this turn.
   * All units on a property you own take 3HP mass damage and you gain the funds value of that HP.
   * Bear: Gives you a fast injection of funds based on 1.2x your liquidated units' HP value. Should probably build before using.
   * Bull: Having units on properties when you use it is poor due to the 0.9x liquidation price, however, the discount should be welcome.
   */
  private static class UpDownTurnAbility extends CommanderAbility implements COModifier
  {
    private static final String UPTURN_NAME = "UpTurn";
    private static final String DOWNTURN_NAME = "DownTurn";
    private static final int DOWNUPTURN_COST = 3;
    private static final int DOWNUPTURN_LIQUIDATION = 3;
    Bear_Bull COcast;

    UpDownTurnAbility(Commander commander)
    {
      // as we start in Bear form, UpTurn is the correct starting name
      super(commander, UPTURN_NAME, DOWNUPTURN_COST);
      COcast = (Bear_Bull) commander;
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

      // Damage is dealt after swapping D2Ds so it's actually useful to Bear
      for( XYCoord xyc : myCommander.ownedProperties )
      {
        Location loc = gameMap.getLocation(xyc);
        if( loc.getOwner() == myCommander )
        {
          Unit victim = loc.getResident();
          if( null != victim )
          {
            double delta = victim.alterHP(-1*DOWNUPTURN_LIQUIDATION); // Remove some of the unit's HP
            myCommander.money += (-1 * delta * victim.model.getCost()) / 10; // ...and turn it into moolah
          }
        }
      }
    }

    @Override // COModifier interface.
    public void applyChanges(Commander commander)
    {
      Bear_Bull cmdr = (Bear_Bull) commander;
      cmdr.swapD2Ds(false);
    }

    @Override
    public void revertChanges(Commander commander)
    {
      Bear_Bull cmdr = (Bear_Bull) commander;
      cmdr.swapD2Ds(true);
    }
  }

  /**
   * Boom/Bust reduces costs by 20% (to 70% or 100%). Swap D2Ds permanently when this wears off.
   * In the future, it'd be cool to be able to build on valid locations next to production facilities... but that's hard and I'm lazy
   * For clarification: Boom is Bear's ability; Bust is Bull's
   */
  private static class BustBoomAbility extends CommanderAbility implements COModifier
  {
    private static final String BUST_NAME = "Bust";
    private static final String BOOM_NAME = "Boom";
    private static final int BOOMBUST_COST = 6;
    private static final double BOOMBUST_BUFF = 0.2;
    Bear_Bull COcast;

    BustBoomAbility(Commander commander)
    {
      // as we start in Bear form, Boom is the correct starting name
      super(commander, BOOM_NAME, BOOMBUST_COST);
      COcast = (Bear_Bull) commander;
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
      Bear_Bull cmdr = (Bear_Bull) commander;
      // Instead of swapping, we get a discount. Yaaaay.
      for( UnitModel um : cmdr.unitModels.values() )
      {
        um.COcost -= BOOMBUST_BUFF;
      }
    }

    @Override // COModifier interface.
    public void revertChanges(Commander commander)
    {
      // Next turn, we swap D2Ds permanently
      Bear_Bull cmdr = (Bear_Bull) commander;
      cmdr.swapD2Ds(true);
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
