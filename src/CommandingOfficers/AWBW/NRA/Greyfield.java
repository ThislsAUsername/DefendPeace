package CommandingOfficers.AWBW.NRA;

import java.util.ArrayList;
import java.util.Arrays;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.UnitActionFactory;
import Engine.GameScenario.GameRules;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;
import Units.UnitModelScheme;
import Units.UnitModelScheme.GameReadyModels;
import lombok.var;

public class Greyfield extends AWBWCommander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator("SBW");
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator(String discriminator)
    {
      super("Greyfield", UIUtils.SourceGames.AWBW, UIUtils.NRA, discriminator);
      infoPages.add(new InfoPage(
            "Greyfield (Sneak By Web)\n"
          + "Alternative Greyfield port for the case that the Seaplane-converting one isn't accepted.\n"
          + "+10/40 stats for naval units, copters, and multirole attack planes (Stealths/Seaplanes)\n"));
      addSharedPages();
    }
    protected void addSharedPages()
    {
      infoPages.add(new InfoPage(new HighCommand(null, null),
            "No special effect.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new SupplyChain(null, null),
            "Resupply ammo, fuel, and materials.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Greyfield(this, rules);
    }
  }

  static final long BOOST_MASK_ANY = UnitModel.SEA | UnitModel.HOVER; // Also some other unit, via below
  final ArrayList<UnitModel> buffablePlanes = new ArrayList<>();
  public Greyfield(CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);

    calcBuffables();

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new HighCommand(this, cb));
    addCommanderAbility(new SupplyChain(this, cb));
  }
  protected void calcBuffables()
  {
    boolean matchOnAny = false;
    long multiRoleJets = UnitModel.JET | UnitModel.AIR_TO_AIR | UnitModel.AIR_TO_SURFACE | UnitModel.ASSAULT;
    buffablePlanes.addAll(getAllModels(multiRoleJets, matchOnAny));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAny(BOOST_MASK_ANY)
        || buffablePlanes.contains(params.attacker.model) )
      params.attackPower += 10;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isAny(BOOST_MASK_ANY)
        || buffablePlanes.contains(params.defender.model) )
      params.defenseSubtraction += 40;
  }

  private static class HighCommand extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "High Command";
    private static final int COST = 1;
    
    HighCommand(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
    }
  }

  private static class SupplyChain extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Supply Chain";
    private static final int COST = 3;

    SupplyChain(Commander commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      for( Unit unit : myCommander.army.getUnits() )
      {
        unit.resupply();
        unit.materials = unit.model.maxMaterials;
      }
    }
  }

  /**
   * Exactly the same as the other Greyfield, except he builds and buffs only Seaplanes D2D instead of any multirole jet.
   */
  public static class SeaplaneAbsolutist extends Greyfield
  {
    private static final long serialVersionUID = 1L;

    private static final CommanderInfo coInfo = new instantiator();
    public static CommanderInfo getInfo()
    {
      return coInfo;
    }
    private static class instantiator extends Greyfield.instantiator
    {
      private static final long serialVersionUID = 1L;
      public instantiator()
      {
        super("BW");
        infoPages.clear();
        infoPages.add(new InfoPage(
              "Greyfield (AWBW)\n"
            + "Builds Seaplanes instead of Stealths, if Stealths are present in the unit set.\n"
            + "Those Seaplanes will be 15k Stealths that can't hide, with 3 ammo, 40 fuel, and +1 move.\n"
            + "+10/40 stats for naval units, copters, and Seaplanes.\n"));
        // Should this variant get the ability to build seaplanes from AWBW Carriers?
        addSharedPages();
      }
      @Override
      public Commander create(GameScenario.GameRules rules)
      {
        return new SeaplaneAbsolutist(this, rules);
      }
      @Override
      public void injectUnits(GameReadyModels grms)
      {
        if( null == UnitModelScheme.getModelFromString(SEAPLANE_NAME, grms.unitModels) )
          grms.unitModels.add(createSeaplaneFromStealth(grms));
      }
    }

    static final String SEAPLANE_NAME = "Seaplane";
    static final String STEALTH_NAME  = "Stealth";
    public SeaplaneAbsolutist(CommanderInfo info, GameRules rules)
    {
      super(info, rules);
      UnitModel stelf = UnitModelScheme.getModelFromString(STEALTH_NAME, unitModels);
      if (null != stelf)
      {
        var airBuyables = unitProductionByTerrain.get(TerrainType.AIRPORT);
        airBuyables.remove(stelf);
        airBuyables.addAll(buffablePlanes);
      }
    }
    @Override
    protected void calcBuffables()
    {
      buffablePlanes.add(UnitModelScheme.getModelFromString(SEAPLANE_NAME, unitModels));
    }

    private static UnitModel createSeaplaneFromStealth(GameReadyModels grms)
    {
      UnitModel stelf = UnitModelScheme.getModelFromString(STEALTH_NAME, grms.unitModels);

      var seaplane = stelf.toBuilder();
      seaplane.name(SEAPLANE_NAME);
      seaplane.costBase(15000);
      // Use the same DS-style charge 'cause "eh"
      seaplane.baseMovePower(stelf.baseMovePower + 1);
      seaplane.maxAmmo(3);
      seaplane.maxFuel(40);
      seaplane.baseActions(new ArrayList<>(Arrays.asList(UnitActionFactory.COMBAT_VEHICLE_ACTIONS)));

      UnitModel builtPlane = seaplane.build();
      builtPlane.setCalculatedProps();

      return builtPlane;
    }
  } // ~SeaplaneAbsolutist

}
