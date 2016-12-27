package CommandingOfficers.Modifiers;

import java.util.ArrayList;

import Terrain.Environment;
import Units.UnitModel;
import CommandingOfficers.Commander;
import Engine.Combat.CombatParameters;

// Provides a damage boost either universally, on one terrain
public class CODamageModifier extends COModifier
{
  private int attackModifier = 0;
  private Environment validTerrains = null;

  public CODamageModifier(Commander user, int percentChange, Environment terrain)
  {
    super(user);
    attackModifier = percentChange;
    validTerrains = terrain;
  }

  public CODamageModifier(Commander user, int percentChange)
  {
    super(user);
    attackModifier = percentChange;
  }
  
  @Override
  public void alterCombat(CombatParameters params)
  {
    if( params.attacker.CO == CO && validTerrains == params.attackTerrain )
    {
      params.attackFactor += attackModifier;
    }
  }

  @Override
  public void apply()
  {
    if( null == validTerrains )
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
    if( null == validTerrains )
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
