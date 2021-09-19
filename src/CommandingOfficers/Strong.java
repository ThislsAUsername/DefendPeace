package CommandingOfficers;

import java.util.ArrayList;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import CommandingOfficers.Modifiers.COModifier.GenericCOModifier;
import Engine.GameScenario;
import Engine.Combat.StrikeParams.BattleParams;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Units.Unit;
import Units.UnitModel;

/**
 * Commander Strong knows that an army's strength lies in its people. Well-positioned soldiers
 * with proper equipment and training are guaranteed to bring results. His focus is therefore on
 * achieving this in his own forces, and denying it of his opponent. As a result, his transport
 * units and boots on the ground are among the best, and he brings some extra anti-personnel
 * firepower to keep his opponents in check.
 */
public class Strong extends Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Strong");
      infoPages.add(new InfoPage(
          "Commander Strong knows that an army's strength lies in its people; soldiers with proper training and equipment are guaranteed to bring results.\n" +
          "His transport units and boots on the ground are among the best, and he brings some extra anti-personnel firepower to keep his opponents in check."));
      infoPages.add(new InfoPage(
          "Passive:\n" + 
          "+"+PASSIVE_INF_BUFF+"% attack for all footsoldiers\n" +
          "+"+PASSIVE_ANTI_INF_BUFF+"% attack vs enemy footsoldiers, when attacking\n" +
          "+1 movement and +1 capacity for all transports\n" +
          "Can build Mechs from air/sea ports\n"));
      infoPages.add(new InfoPage(
          "Strongarm ("+StrongArmAbility.STRONGARM_COST+"):\n" +
          "+"+StrongArmAbility.STRONGARM_BUFF+"% attack and defense for all units\n" +
          "+"+StrongArmAbility.STRONGARM_FOOT_BUFF+"% attack for footsoldiers\n" +
          "+2 movement for footsoldiers and APCs\n" +
          "Can build infantry on air/sea ports\n"));
      infoPages.add(new InfoPage(
          "Mobilize ("+MobilizeAbility.MOBILIZE_COST+"):\n" + 
          "+"+MobilizeAbility.MOBILIZE_BUFF+"% attack for all units\n" +
          "+"+MobilizeAbility.MOBILIZE_DEFENSE_BUFF+"% defense for all units\n" +
          "+2 movement for all units\n" +
          "All footsoldiers can move again\n" +
          "Can build footsoldiers on cities, industries, and the HQ\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Strong(rules);
    }
  }

  private static final int PASSIVE_INF_BUFF = 15;
  private static final int PASSIVE_ANTI_INF_BUFF = 15;

  public Strong(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    // Strong allows mechs to be built from any production building.
    UnitModel mechModel = getUnitModel(UnitModel.MECH);
    UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, mechModel);
    upm.addProductionPair(TerrainType.SEAPORT, mechModel);
    upm.applyChanges(this); // Passive ability, so don't add it to the COModifier list; just apply it and forget it.

    // Give Strong's footies a base damage buff.
    CODamageModifier strongMod = new CODamageModifier(PASSIVE_INF_BUFF); // Give us a nice base power boost.
    for( UnitModel model : getAllModels(UnitModel.TROOP) )
      strongMod.addApplicableUnitModel(model);
    strongMod.applyChanges(this); // Passive ability, so don't add it to the COModifier list; just apply it and forget it.

    // Give every transport type extra move range and an extra cargo slot.
    for (UnitModel model : unitModels)
    {
      if( model.holdingCapacity > 0 )
      {
        model.movePower++;
        model.holdingCapacity++;
      }
    }

    addCommanderAbility(new StrongArmAbility());
    addCommanderAbility(new MobilizeAbility());
  }

  /**
   * Strong gets a little extra oomph when fighting enemy foot soldiers.
   */
  @Override
  public void modifyUnitAttackOnUnit(BattleParams params)
  {
    // Grant a firepower increase if we are attacking and the defender is on foot.
    if( (params.attacker.unit.CO == this) && params.defender.unit.model.isTroop() )
    {
      params.attackPower += PASSIVE_ANTI_INF_BUFF;
    }
  }

  /**
   * StrongArm grants Strong a firepower and defense bonus, additional mobility,
   * and the ability to build infantry units from all production buildings.
   */
  private static class StrongArmAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String STRONGARM_NAME = "Strongarm";
    private static final int STRONGARM_COST = 4;
    private static final int STRONGARM_BUFF = 10;
    private static final int STRONGARM_FOOT_BUFF = 20; // On top of STONGARM_BUFF

    StrongArmAbility()
    {
      super(STRONGARM_NAME, STRONGARM_COST);
    }

    @Override
    protected void enqueueCOMods(Commander myCommander, MapMaster gameMap, ArrayList<COModifier> modList)
    {
      GenericCOModifier damageMod = new CODamageModifier(STRONGARM_BUFF);
      GenericCOModifier defenseMod = new CODefenseModifier(STRONGARM_BUFF);
      GenericCOModifier damageModTroop = new CODamageModifier(STRONGARM_FOOT_BUFF);
      damageModTroop.addApplicableUnitModels(myCommander.getAllModels(UnitModel.TROOP));
      modList.add(damageMod);
      modList.add(defenseMod);
      modList.add(damageModTroop);

      // Make infantry buildable from all production buildings.
      UnitModel infModel = myCommander.getUnitModel(UnitModel.TROOP);
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, infModel);
      upm.addProductionPair(TerrainType.SEAPORT, infModel);
      modList.add(upm);

      // Grant troops and transports additional movement power.
      COMovementModifier moveMod = new COMovementModifier(2);
      for( UnitModel model : myCommander.getAllModels(UnitModel.TROOP | UnitModel.TRANSPORT) )
      {
        moveMod.addApplicableUnitModel(model);
      }
      modList.add(moveMod);
    }

    @Override
    protected void perform(Commander myCommander, MapMaster gameMap)
    {}
  }

  /**
   * Mobilize grants Strong a firepower bonus, additional mobility,
   * and the ability to build infantry units from any building he owns.
   */
  private static class MobilizeAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String MOBILIZE_NAME = "Mobilize";
    private static final int MOBILIZE_COST = 8;
    private static final int MOBILIZE_BUFF = 40;
    private static final int MOBILIZE_DEFENSE_BUFF = 10;

    MobilizeAbility()
    {
      super(MOBILIZE_NAME, MOBILIZE_COST);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void enqueueCOMods(Commander myCommander, MapMaster gameMap, ArrayList<COModifier> modList)
    {
      // Grant the base firepower/defense bonus.
      GenericCOModifier damageMod = new CODamageModifier(MOBILIZE_BUFF);
      GenericCOModifier defenseMod = new CODefenseModifier(MOBILIZE_DEFENSE_BUFF);
      modList.add(damageMod);
      modList.add(defenseMod);

      // Make all TROOPs buildable from all production centers, cities, and the HQ.
      UnitProductionModifier upm = new UnitProductionModifier();
      for( UnitModel model : myCommander.getAllModels(UnitModel.TROOP) )
      {
        upm.addProductionPair(TerrainType.AIRPORT, model);
        upm.addProductionPair(TerrainType.SEAPORT, model);
        upm.addProductionPair(TerrainType.CITY, model);
        upm.addProductionPair(TerrainType.HEADQUARTERS, model);
      }
      modList.add(upm);

      // Grant a global +2 movement buff.
      COMovementModifier moveMod = new COMovementModifier(2);
      for( UnitModel model : myCommander.unitModels )
      {
        moveMod.addApplicableUnitModel(model);
      }
      modList.add(moveMod);

      // Lastly, all troops are refreshed and able to move again.
      for( Unit unit : myCommander.units )
      {
        if( unit.model.isAll(UnitModel.TROOP) )
        {
          unit.isTurnOver = false;
        }
      }
    }

    @Override
    protected void perform(Commander myCommander, MapMaster gameMap)
    {}
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
