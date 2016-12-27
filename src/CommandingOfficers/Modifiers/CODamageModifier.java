package CommandingOfficers.Modifiers;

import Terrain.Environment;
import Units.UnitModel;
import CommandingOfficers.Commander;
import Engine.Combat.CombatParameters;

/** Provides a damage boost either universally, on one terrain */
public class CODamageModifier extends COModifier
{
  private int attackModifier = 0;
  private Environment requiredTerrain = null;

  public CODamageModifier(Commander user, int percentChange, Environment terrain)
  {
    super(user);
    attackModifier = percentChange;
    requiredTerrain = terrain;
  }

  public CODamageModifier(Commander user, int percentChange)
  {
    super(user);
    attackModifier = percentChange;
  }

  @Override
  public void alterCombat(CombatParameters params)
  {
    if( params.attacker.CO == CO && requiredTerrain == params.attackTerrain )
    {
      params.attackFactor += attackModifier;
    }
  }

  @Override
  public void apply()
  {
    if( null == requiredTerrain )
    {
      for( UnitModel um : CO.unitModels )
      {
        if( um.weaponModels != null )
        {
          um.modifyDamageRatio(attackModifier);
        }
      }
    }
  }

  @Override
  public void revert()
  {
    if( null == requiredTerrain )
    {
      for( UnitModel um : CO.unitModels )
      {
        if( um.weaponModels != null )
        {
          um.modifyDamageRatio(-attackModifier);
        }
      }
    }
  }
}
