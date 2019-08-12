package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import CommandingOfficers.Modifiers.CODefenseModifier;
import CommandingOfficers.Modifiers.COMovementModifier;
import CommandingOfficers.Modifiers.UnitProductionModifier;
import Engine.GameScenario;
import Engine.Combat.BattleInstance.BattleParams;
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
          "Commander Strong knows that an army's strength lies in its people.\n" +
          "Well-positioned soldiers with proper equipment and training are guaranteed to bring results.\n" +
          "His focus is therefore on achieving this in his own forces, and denying it of his opponent.\n" +
          "As a result, his transport units and boots on the ground are among the best, and he brings some extra anti-personnel firepower to keep his opponents in check."));
      infoPages.add(new InfoPage(
          "Passive:\n" + 
          "- Strong's footsoldiers get an attack bonus of 15%\n" +
          "- When attacking footsoldiers, all of Strong's units get an attack bonus of 15%\n" +
          "- Strong's transports move 1 space further and can hold one more unit than average\n" +
          "- Strong can build Mechs from air/sea ports\n"));
      infoPages.add(new InfoPage(
          "Strongarm ("+StrongArmAbility.STRONGARM_COST+"):\n" +
          "Gives an attack and defense boost of "+StrongArmAbility.STRONGARM_BUFF+"%\n" +
          "Gives footsoldiers an additional "+StrongArmAbility.STRONGARM_FOOT_BUFF+"% attack\n" +
          "Grants footsoldiers and APCs two extra points of movement\n" +
          "Allows Strong to build infantry on air/sea ports\n"));
      infoPages.add(new InfoPage(
          "Mobilize ("+MobilizeAbility.MOBILIZE_COST+"):\n" + 
          "Gives an attack boost of "+MobilizeAbility.MOBILIZE_BUFF+"%\n" +
          "Gives a defense boost of "+MobilizeAbility.MOBILIZE_DEFENSE_BUFF+"%\n" +
          "Grants two extra points of movement\n" +
          "Refreshes footsoldiers\n" +
          "Allows Strong to build footsoldiers on cities, industries, and the HQ\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Strong(rules);
    }
  }

  public Strong(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    // Strong allows infantry to be built from any production building.
    UnitModel mechModel = getUnitModel(UnitModel.UnitEnum.MECH);
    UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, mechModel);
    upm.addProductionPair(TerrainType.SEAPORT, mechModel);
    upm.applyChanges(this); // Passive ability, so don't add it to the COModifier list; just apply it and forget it.

    // Give Strong's footies a base damage buff. This COModifier is not added
    // to the modifiers collection so it will not be reverted.
    CODamageModifier strongMod = new CODamageModifier(15); // Give us a nice base power boost.
    strongMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.INFANTRY));
    strongMod.addApplicableUnitModel(getUnitModel(UnitModel.UnitEnum.MECH));
    strongMod.applyChanges(this);

    // Give every transport type extra move range and an extra cargo slot.
    for( UnitModel.UnitEnum umEnum : UnitModel.UnitEnum.values() )
    {
      UnitModel model = getUnitModel(umEnum);
      if( model.holdingCapacity > 0 )
      {
        model.movePower++;
        model.holdingCapacity++;
      }
    }

    addCommanderAbility(new StrongArmAbility(this));
    addCommanderAbility(new MobilizeAbility(this));
  }

  /**
   * Strong gets a little extra oomph when fighting enemy foot soldiers.
   */
  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    // Grant a firepower increase if we are attacking and the defender is on foot.
    if( (params.attacker.CO == this) && (params.defender.model.chassis == UnitModel.ChassisEnum.TROOP) )
    {
      params.attackFactor += 15;
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

    CODamageModifier damageMod = null;
    CODefenseModifier defenseMod = null;
    CODamageModifier damageModTroop = null;

    StrongArmAbility(Commander commander)
    {
      super(commander, STRONGARM_NAME, STRONGARM_COST);

      damageMod = new CODamageModifier(STRONGARM_BUFF);
      defenseMod = new CODefenseModifier(STRONGARM_BUFF);
      damageModTroop = new CODamageModifier(STRONGARM_FOOT_BUFF);
      for( UnitModel model : commander.unitModels.values() )
      {
        if( model.chassis == UnitModel.ChassisEnum.TROOP )
        {
          damageModTroop.addApplicableUnitModel(model);
        }
      }
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Grant the base firepower/defense bonus.
      myCommander.addCOModifier(damageMod);
      myCommander.addCOModifier(defenseMod);
      myCommander.addCOModifier(damageModTroop);

      // Make infantry buildable from all production buildings.
      UnitModel infModel = myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY);
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, infModel);
      upm.addProductionPair(TerrainType.SEAPORT, infModel);
      myCommander.addCOModifier(upm);

      // Grant troops and transports additional movement power.
      COMovementModifier moveMod = new COMovementModifier(2);
      for( UnitModel model : myCommander.unitModels.values() )
      {
        if( (model.chassis == UnitModel.ChassisEnum.TROOP) || (model.holdingCapacity > 0))
        {
          moveMod.addApplicableUnitModel(model);
        }
      }
      myCommander.addCOModifier(moveMod);
    }
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

    CODamageModifier damageMod = null;
    CODefenseModifier defenseMod = null;

    MobilizeAbility(Commander commander)
    {
      super(commander, MOBILIZE_NAME, MOBILIZE_COST);

      damageMod = new CODamageModifier(MOBILIZE_BUFF);
      defenseMod = new CODefenseModifier(MOBILIZE_DEFENSE_BUFF);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      // Grant the base firepower/defense bonus.
      myCommander.addCOModifier(damageMod);
      myCommander.addCOModifier(defenseMod);

      // Make inf/mechs buildable from all buildings.
      UnitModel infModel = myCommander.getUnitModel(UnitModel.UnitEnum.INFANTRY);
      UnitModel mechModel = myCommander.getUnitModel(UnitModel.UnitEnum.MECH);
      UnitProductionModifier upm = new UnitProductionModifier(TerrainType.AIRPORT, infModel);
      upm.addProductionPair(TerrainType.SEAPORT, infModel);
      upm.addProductionPair(TerrainType.CITY, mechModel);
      upm.addProductionPair(TerrainType.CITY, infModel);
      upm.addProductionPair(TerrainType.HEADQUARTERS, mechModel);
      upm.addProductionPair(TerrainType.HEADQUARTERS, infModel);
      myCommander.addCOModifier(upm);

      // Grant a global +2 movement buff.
      COMovementModifier moveMod = new COMovementModifier(2);
      for( UnitModel model : myCommander.unitModels.values() )
      {
        moveMod.addApplicableUnitModel(model);
      }
      myCommander.addCOModifier(moveMod);

      // Lastly, all troops are refreshed and able to move again.
      for( Unit unit : myCommander.units )
      {
        if( unit.model.chassis == UnitModel.ChassisEnum.TROOP )
        {
          unit.isTurnOver = false;
        }
      }
    }
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
