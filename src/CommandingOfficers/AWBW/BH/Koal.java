package CommandingOfficers.AWBW.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;

public class Koal extends AWBWCommander
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
      super("Koal", UIUtils.SourceGames.AWBW, UIUtils.BH);
      infoPages.add(new InfoPage(
            "Koal (AWBW)\n"
          + "Units (even air units) gain +10% attack power on roads."));
      infoPages.add(new InfoPage(new ForcedMarch(null, null),
            "All units gain +1 movement, and the road bonus is increased to +20%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new TrailOfWoe(null, null),
            "All units gain +2 movement, and the road bonus is increased to +30%.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new Koal(rules);
    }
  }

  public Koal(GameScenario.GameRules rules)
  {
    super(coInfo, rules);

    CommanderAbility.CostBasis cb = getGameBasis();
    addCommanderAbility(new ForcedMarch(this, cb));
    addCommanderAbility(new TrailOfWoe(this, cb));
  }

  @Override
  public void modifyUnitAttack(StrikeParams params)
  {
    if( null == params.attacker.env )
      return;
    if( params.attacker.env.terrainType == TerrainType.ROAD )
      params.attackPower += 10;
  }
  public static class RoadHitMod implements UnitModifierWithDefaults
  {
    private static final long serialVersionUID = 1L;
    int power;
    public RoadHitMod(int power)
    {
      this.power = power;
    }
    @Override
    public void modifyUnitAttack(StrikeParams params)
    {
      if( null == params.attacker.env )
        return;
      if( params.attacker.env.terrainType == TerrainType.ROAD )
        params.attackPower += power;
    }
  }

  private static class ForcedMarch extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Forced March";
    private static final int COST = 3;
    UnitModifier moveMod, hitMod;

    ForcedMarch(Koal commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitMovementModifier(1);
      hitMod = new RoadHitMod(10);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(hitMod);
    }
  }

  private static class TrailOfWoe extends AWBWAbility
  {
    private static final long serialVersionUID = 1L;
    private static final String NAME = "Trail of Woe";
    private static final int COST = 5;
    UnitModifier moveMod, hitMod;

    TrailOfWoe(Koal commander, CostBasis basis)
    {
      super(commander, NAME, COST, basis);
      moveMod = new UnitMovementModifier(2);
      hitMod = new RoadHitMod(20);
    }

    @Override
    protected void enqueueMods(MapMaster gameMap, ArrayList<UnitModifier> modList)
    {
      modList.add(moveMod);
      modList.add(hitMod);
    }
  }

}
