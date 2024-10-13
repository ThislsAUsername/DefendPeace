package CommandingOfficers.AW3.BH;

import java.util.ArrayList;
import CommandingOfficers.*;
import CommandingOfficers.AW3.AW3Commander;
import Engine.GameScenario;
import Engine.Combat.StrikeParams;
import Engine.UnitMods.UnitModifier;
import Engine.UnitMods.UnitModifierWithDefaults;
import Engine.UnitMods.UnitMovementModifier;
import UI.UIUtils;
import Terrain.MapMaster;
import Terrain.TerrainType;

public class Koal extends AW3Commander
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
      super("Koal", UIUtils.SourceGames.AW3, UIUtils.BH, "");
      infoPages.add(new InfoPage(
            "Koal (AW3)\n"
          + "A CO of the Black Hole army who is always planning his next destructive act.\n"
          + "He's a master of road based battles. His CO powers cost less to use than those of other COs.\n"
          + "(+10 road attack)"));
      infoPages.add(new InfoPage(new ForcedMarch(null, null),
            "Movement range for all units is increased by one space. Units have more firepower (+10) on roads.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(new TrailOfWoe(null, null),
            "Movement for all units is increased by two spaces. Units have more firepower (+20) on roads.\n"
          + "+10 attack and defense.\n"));
      infoPages.add(new InfoPage(
            "Hit: Proverbs, Ramen\n"
          + "Miss: Fondue"));
      infoPages.add(AW3_MECHANICS_BLURB);
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

  private static class ForcedMarch extends AW3Ability
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

  private static class TrailOfWoe extends AW3Ability
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
