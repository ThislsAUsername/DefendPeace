package CommandingOfficers;

import Engine.GameScenario;

import CommandingOfficers.CommanderInfo.InfoPage;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Terrain.MapMaster;
import Units.Unit;
import Units.UnitContext;

public abstract class RuinedCommander extends DeployableCommander
{
  private static final long serialVersionUID = 1L;
  public static final InfoPage ZONE_MECHANICS_BLURB = new InfoPage(
            "Days of Ruin mechanics:\n"
          + "You have a CO zone that covers the area around your COU, and grants bonuses to your units inside.\n"
          + "The zone expands 1 tile when your meter is half full, and again at full charge.\n"
          + "All units get +10 attack and +10 Damage Division (DD) while in the zone.\n"
          + "Your CO may also have another zone boost that stacks with that boost, but may only apply to some unit types.\n"
          + "The zone becomes global while your power is active, but activating your power requires your COU's action.\n"
          + "CO Energy gained reflects HP damage you deal, and not on the type of unit you hit.\n"
          + "You can only gain CO energy for combat where the initiator is inside the zone.\n"
          + "  (This means an enemy in your zone attacking outside your zone can give you charge from your counterattack)\n"
          + "However, your CO ability does not increase in cost as you use it.\n"
          + "Veterancy:\n"
          + "On making a kill, your unit will level up.\n"
          + "There are 3 veterancy levels; their stat boosts are +5/0, +10/0, and +20/20 attack/DD.\n"
          + "Your COU is always max level.\n"
          + "Veterancy bonuses do not apply if this CO is tagged out\n"
          );

  public final int zonePow;
  public final int zoneDef;
  public final int zoneBaseRadius;
  public int zoneRadius;
  public boolean zoneIsGlobal = false;

  @Override
  public int getCOUCount() {return 1;}
  @Override
  public void onCOULost(Unit minion)
  {
    modifyAbilityPower(-42);
    zoneIsGlobal = false;
  }

  public RuinedCommander(int atk, int def, int radius, CommanderInfo info, GameScenario.GameRules rules)
  {
    super(info, rules);
    zonePow = atk;
    zoneDef = def;
    zoneBaseRadius = radius;
    zoneRadius = radius;
  }

  @Override
  public void modifyActionList(UnitContext uc)
  {
//    if( COUs.contains(uc.unit) )
      // TODO power activation
//      uc.actionTypes.add(new DeployCOU(this));
    super.modifyActionList(uc);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( COUs.contains(params.attacker.unit) )
      params.attackPower += zonePow;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    // TODO: DD, zone, vets
    if( COUs.contains(params.defender.unit) )
      params.defensePower += zoneDef;
  }

  protected static class RuinedAbility extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    RuinedCommander COcast;

    protected RuinedAbility(RuinedCommander commander, String name, int cost)
    {
      super(commander, name, cost);
      COcast = commander;
    }

    @Override
    protected void adjustCost() {}
    
    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.zoneIsGlobal = true;
    }

    @Override
    protected void revert(MapMaster gameMap)
    {
      COcast.zoneIsGlobal = false;
    }
  }

  //////////////////////////////////////////////////////////
  // Action definition happens after this point
  //////////////////////////////////////////////////////////

}
