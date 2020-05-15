package CommandingOfficers.IDS;
import Engine.GameScenario;
import CommandingOfficers.Commander;
import CommandingOfficers.CommanderInfo;
import Engine.GameEvents.GameEvent;
import Engine.GameEvents.GameEventListener;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.GlobalWeatherEvent;
import Terrain.MapMaster;
import Terrain.TerrainType;
import Terrain.Environment.Weathers;
import Units.Unit;
import Units.UnitModel;

public class OmegaCaulder extends TabithaEngine
{
  private static final long serialVersionUID = 1L;
  public static final int MEGA_ATK = 70;
  public static final int MEGA_DEF = 45;
  public static final int D2DREPAIRS = 5;
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    private static final long serialVersionUID = 1L;
    public instantiator()
    {
      super("Omega Caulder");
      infoPages.add(new InfoPage(
            "Called \'Omega\' because he's extra fair and balanced.\n"
          + "Can grant up to 3 \'Mega Boost\'s of +"+MEGA_ATK+"/"+MEGA_DEF+" stats; this power-up lasts until next turn.\n"
          + "All damaged units are repaired for +"+ D2DREPAIRS +" HP every turn (liable for costs).\r\n"
          + "Unaffected by weather."));
      infoPages.add(MECHANICS_BLURB);
      infoPages.add(new InfoPage(
            "APOCALYPSE (13):\n"
          + "I heard you like pain?"));
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new OmegaCaulder(rules);
    }
  }

  @Override
  public int getMegaBoostCount() {return 3;}

  public OmegaCaulder(GameScenario.GameRules rules)
  {
    super(MEGA_ATK, MEGA_DEF, coInfo, rules);

    for( UnitModel um : unitModels )
    {
      for( TerrainType terrain : TerrainType.TerrainTypeList )
      {
        um.propulsion.setMoveCost(Weathers.RAIN, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
        um.propulsion.setMoveCost(Weathers.SNOW, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
        um.propulsion.setMoveCost(Weathers.SANDSTORM, terrain, um.propulsion.getMoveCost(Weathers.CLEAR, terrain));
      }
    }

    addCommanderAbility(new APOCALYPSE(this));
  }

  protected static class APOCALYPSE extends NukeIt
  {
    private static final long serialVersionUID = 1L;

    APOCALYPSE(TabithaEngine commander)
    {
      super(commander, "APOCALYPSE", 13, 8, 50, 35);
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      super.perform(gameMap);
      GameEvent event = new GlobalWeatherEvent(Weathers.SNOW, 3);
      event.performEvent(gameMap);
      GameEventListener.publishEvent(event);
    }
  }

  @Override
  public GameEventQueue initTurn(MapMaster map)
  {
    GameEventQueue ret = super.initTurn(map);

    for( Unit unit : units )
    {
      double HP = unit.getPreciseHP();
      double maxHP = unit.model.maxHP;
      if( HP < maxHP )
      {
        int neededHP = (int) Math.min(maxHP - unit.getHP(), D2DREPAIRS);
        double proportionalCost = unit.model.getCost() / maxHP;
        int repairedHP = neededHP;
        while (money < repairedHP * proportionalCost)
        {
          repairedHP--;
        }
        money -= repairedHP * proportionalCost;
        unit.alterHP(repairedHP);

        // Top off HP if there's excess power but we hit the HP cap
        if (repairedHP < D2DREPAIRS && unit.getHP() == maxHP)
          unit.alterHP(1);
      }
    }
    
    return ret;
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }
}
