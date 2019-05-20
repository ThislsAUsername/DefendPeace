package CommandingOfficers;

import CommandingOfficers.Modifiers.CODamageModifier;
import Engine.XYCoord;
import Engine.Combat.BattleInstance.BattleParams;
import Engine.GameEvents.GameEventQueue;
import Terrain.MapMaster;
import Terrain.GameMap;
import Terrain.Location;
import Terrain.TerrainType;
import Units.Unit;

public class BHKindle extends Commander
{
  private static final CommanderInfo coInfo = new instantiator();
  private static class instantiator extends CommanderInfo
  {
    public instantiator()
    {
      super("Kindle");
      infoPages.add(new InfoPage(
          "Kindle\r\n" + 
          "  Units' (even aircraft) attack power is increased by +40% while on urban terrain (HQs, bases, (air-)ports, cities, labs, and comtowers only)\r\n" + 
          "Urban Blight -- -3HP to all enemy units on urban terrain; Urban bonus increased to +80%\r\n" + 
          "High Society -- Urban bonus increased to +130%, and attack power boosted by +3% for every city owned"));
    }
    @Override
    public Commander create()
    {
      return new BHKindle();
    }
  }

  private int urbanBuff = 40;

  public BHKindle()
  {
    super(coInfo);

    addCommanderAbility(new UrbanBlight(this));
    addCommanderAbility(new HighSociety(this));
  }

  public static CommanderInfo getInfo()
  {
    return coInfo;
  }

  @Override
  public GameEventQueue initTurn(GameMap map)
  {
    this.urbanBuff = 40;
    return super.initTurn(map);
  }

  @Override
  public void applyCombatModifiers(BattleParams params, boolean amITheAttacker)
  {
    Unit minion = null;
    if( params.attacker.CO == this )
    {
      minion = params.attacker;
    }

    if( null != minion )
    {
      Location loc = params.combatRef.gameMap.getLocation(params.combatRef.attackerX, params.combatRef.attackerY);
      if( loc != null && loc.isCaptureable() )
      {
        params.attackFactor += urbanBuff;
      }
    }
  }

  private static class UrbanBlight extends CommanderAbility
  {
    private static final String NAME = "Urban Blight";
    private static final int COST = 3;
    private static final int VALUE = 3;
    BHKindle COcast;

    UrbanBlight(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BHKindle) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.urbanBuff = 80;
      for( int i = 0; i < gameMap.mapWidth; i++ )
      {
        for( int j = 0; j < gameMap.mapHeight; j++ )
        {
          Location loc = gameMap.getLocation(i, j);
          Unit victim = loc.getResident();
          if( loc.isCaptureable() && victim != null && myCommander.isEnemy(victim.CO) )
            victim.alterHP(-VALUE);
        }
      }
    }
  }

  private static class HighSociety extends CommanderAbility
  {
    private static final String NAME = "High Society";
    private static final int COST = 6;
    BHKindle COcast;

    HighSociety(Commander commander)
    {
      super(commander, NAME, COST);
      COcast = (BHKindle) commander;
    }

    @Override
    protected void perform(MapMaster gameMap)
    {
      COcast.urbanBuff = 130;

      int cityCount = 0;
      for( XYCoord xyc : COcast.ownedProperties )
      {
        Location loc = gameMap.getLocation(xyc);
        if( loc.getEnvironment().terrainType == TerrainType.CITY )
          cityCount++;
      }
      myCommander.addCOModifier(new CODamageModifier(3 * cityCount));
    }
  }
}

