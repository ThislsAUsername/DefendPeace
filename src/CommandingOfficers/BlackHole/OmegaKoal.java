package CommandingOfficers.BlackHole;

import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.Combat.StrikeParams.BattleParams;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderAbility;
import CommandingOfficers.CommanderInfo;
import CommandingOfficers.Modifiers.COMovementModifier;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;

public class OmegaKoal extends Commander
{
  private static final long serialVersionUID = 1L;
  private static final CommanderInfo coInfo = new instantiator();

  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;

    public instantiator()
    {
      super("Omega Koal");
      infoPages.add(new InfoPage("Called \"Omega\" because he's extra fair and balanced.\n"
          + "  Units (even aircraft) act like AW:DS Kanbei on 0-star terrain\r" + "  Boosted units +20/20 stats\r" + "xxxXX\n"
          + "Forced March -- All units gain +1 move; Boosted units +30/0 stats\r\n"
          + "Trail of Woe -- All units gain +2 move; Boosted units +30/30 stats and 2x strength counterattacks\r\n"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new OmegaKoal(rules);
    }
  }

  public static final int basePow = 20; // static values that define the power the COU should stay at
  public static final int baseDef = 20;
  private int nowPow = basePow; // floating values that dip on powers to match the above
  private int nowDef = baseDef;

  public OmegaKoal(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    addCommanderAbility(new RoadRage(this, "Forced March", 3, 1, 30, 0));
    addCommanderAbility(new RoadRage(this, "Trail of Woe", 5, 2, 30, 30));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    this.nowPow = basePow;
    this.nowDef = baseDef;
    return super.initTurn(map);
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( params.attackerTerrainStars < 1 )
      params.attackPower += nowPow;
  }
  @Override
  public void modifyUnitDefenseAgainstUnit(BattleParams params)
  {
    if( params.terrainStars < 1 )
      params.defensePower += nowDef;
  }

  private static class RoadRage extends CommanderAbility
  {
    private static final long serialVersionUID = 1L;
    private final int ROAD_POWER;
    private final int ROAD_DEF;
    private final int MOVE_BUFF;
    OmegaKoal COcast;

    RoadRage(Commander commander, String name, int cost, int move, int roadPower, int roadDef)
    {
      super(commander, name, cost);
      COcast = (OmegaKoal) commander;
      MOVE_BUFF = move;
      ROAD_POWER = roadPower;
      ROAD_DEF = roadDef;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.nowPow += ROAD_POWER;
      COcast.nowDef += ROAD_DEF;
      COcast.addCOModifier(new COMovementModifier(MOVE_BUFF));
    }
  }

}
