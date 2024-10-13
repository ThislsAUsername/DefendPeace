package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.GameEvents.GameEventQueue;
import Engine.GameEvents.MassDamageEvent;
import Engine.UnitMods.UnitDamageModifier;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import UI.UIUtils;
import Units.Unit;
import lombok.var;
import Terrain.MapMaster;

public class Kindle extends AWBWCommander
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
      super("Kindle", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Kindle (AWBW)\n"
          + "Units (even air units) gain +40% attack while on urban terrain. HQs, bases, airports, ports, cities, labs, and comtowers count as urban terrain.\n"));
      infoPages.add(new InfoPage(new UrbanBlight(null, null),
            "All enemy units lose -3 HP on urban terrain. Urban bonus is increased to +80%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new HighSociety(null, null),
            "Urban bonus is increased to +130%, and attack for all units is increased by +3% for each of your owned urban terrain.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Kindle(rules);
    }
  }

  public Kindle(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new UrbanBlight(this, cb));
    addCommanderAbility(new HighSociety(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( null == params.attacker.env )
      return;
    if( params.attacker.env.terrainType.isCapturable() )
      params.attackPower += 40;
  }
  public static class UrbanHitMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;
    int power;
    public UrbanHitMod(int power)
    {
      this.power = power;
    }
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      if( null == params.attacker.env )
        return;
      if( params.attacker.env.terrainType.isCapturable() )
        params.attackPower += power;
    }
  }

  private static class UrbanBlight extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Urban Blight";
    private static final int COST = 3;
    UnitModifier hitMod;

    UrbanBlight(Kindle commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      hitMod = new UrbanHitMod(40);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(hitMod);
    }

    @Override
    public GameEventQueue getEvents(MapMaster map)
    {
      ArrayList<Unit> victims = new ArrayList<>();

      for( int y = 0; y < map.mapHeight; y++ )
      {
        for( int x = 0; x < map.mapWidth; x++ )
        {
          Unit resi = map.getResident(x, y);
          if( resi != null )
            if( myCommander.isEnemy(resi.CO) )
              if( map.getEnvironment(x, y).terrainType.isCapturable() )
                victims.add(resi);
        }
      }
      var damage = new MassDamageEvent(myCommander, victims, 30, false);

      GameEventQueue events = new GameEventQueue();
      events.add(damage);

      return events;
    }
  }

  private static class HighSociety extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "High Society";
    private static final int COST = 6;
    UnitModifier hitMod;

    HighSociety(Kindle commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      hitMod = new UrbanHitMod(90);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(hitMod);
      int propCount = myCommander.army.getOwnedProperties().size();
      modList.add(new UnitDamageModifier(propCount * 3));
    }
  }

}
