package CommandingOfficers.AW1.GE;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW1.AW1Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitContext;

public class Drake extends AW1Commander
{
  private static final long serialVersionUID = 1L;

  private static final CommanderInfo coInfo = new instantiator();
  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Drake", UIUtils.SourceGames.AW1, UIUtils.GE);
      infoPages.add(new InfoPage(
            "Drake (AW1)\n"
          + "A relaxed and carefree swashbuckler.\n"
          + "The sea is his domain. Naval units have top firepower and movement. Air units are weak.\n"
          + "(+1 move and +2 terrain stars for naval, -20 attack for air, moves normally in rain)\n"));
      infoPages.add(new InfoPage(new Tsunami(null),
            "Deals 1 damage to enemy units that aren't in a transport.\n"
          + "1.1x/0.9x damage dealt/taken.\n"));
      infoPages.add(new InfoPage(
            "Hit: The Sea\n"
          + "Miss: Heights"));
      infoPages.add(AW1_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Drake(rules);
    }
  }

  public Drake(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new Tsunami(this));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attacker.model.isAirUnit() )
      params.attackPower -= 20;
  }
  @Override
  public void modifyMovePower(UnitContext uc)
  {
    if( uc.model.isSeaUnit() )
      uc.movePower += 1;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.defender.model.isSeaUnit() )
      params.terrainStars += 2;
  }
  @Override
  public void modifyMoveType(UnitContext uc)
  {
    for( TerrainType terrain : TerrainType.TerrainTypeList )
    {
      uc.moveType.setMoveCost(Weathers.RAIN, terrain, uc.moveType.getMoveCost(Weathers.CLEAR, terrain));
    }
  }

  private static class Tsunami extends AW1BasicAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Tsunami";
    private static final int COST = 8;

    Tsunami(Drake commander)
    {
      super(commander, NAME, COST);
      AIFlags = PHASE_TURN_START | PHASE_TURN_END;
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      ArrayList<Unit> victims = new ArrayList<>();

      for( int x = 0; x < map.mapWidth; x++ )
      {
        for( int y = 0; y < map.mapHeight; y++ )
        {
          Unit resi = map.getResident(x, y);
          if( resi != null && myCommander.isEnemy(resi.CO) )
            victims.add(resi);
        }
      }

      GameEvent damage = new MassDamageEvent(myCommander, victims, 10, false);

      GameEventQueue events = new GameEventQueue();
      events.add(damage);

      return events;
    }
  }

}
