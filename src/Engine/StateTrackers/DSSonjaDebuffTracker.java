package Engine.StateTrackers;

import java.util.HashMap;
import java.util.HashSet;
import Engine.Army;
import Engine.GameInstance;
import Engine.Combat.CombatContext;
import Engine.GameEvents.GameEventQueue;
import Engine.UnitMods.UnitModifierWithDefaults;
import Terrain.MapMaster;
import Units.UnitContext;
import lombok.var;

public class DSSonjaDebuffTracker extends StateTracker implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;

  // Stores the debuff strength to be applied to each army this turn; recalculated on turn change and Sonja (S)COP, since cart Sonja's debuff lasts to the end of the turn she's routed [original research].
  public HashMap<Army, Integer> debuffMap = new HashMap<>();
  public HashSet<CommandingOfficers.AW3.YC.Sonja> debuffers = new HashSet<>();

  public void registerForEvents(GameInstance gi)
  {
    super.registerForEvents(gi);
    for( Army a : gi.armies )
    {
      debuffMap.put(a, 0);
      a.addUnitModifier(this);
    }
  }

  @Override
  public GameEventQueue receiveTurnInitEvent(MapMaster map, Army army, int turn)
  {
    recalcDebuffs();
    return null;
  }

  public void recalcDebuffs()
  {
    // Zero debuffs
    for( Army a : game.armies )
      debuffMap.put(a, 0);
    // Add 'em up
    for( var sonja : debuffers )
    {
      if( sonja.army.isDefeated )
        continue; // Dead people have minimal ability to bully people they aren't fighting
      for( Army a : game.armies )
        if( sonja.isEnemy(a) )
          debuffMap.put(a, debuffMap.get(a) + sonja.terrainDebuff);
    }
  }

  @Override
  public void changeCombatContext(CombatContext instance, UnitContext buffOwner)
  {
    if( instance.defender == buffOwner )
      return; // Apply only once per combat; everyone has this mod, so use the attacker to get in as early as possible.
    instance.attacker.terrainStars -= debuffMap.get(instance.attacker.CO.army);
    instance.defender.terrainStars -= debuffMap.get(instance.defender.CO.army);
  }
}
