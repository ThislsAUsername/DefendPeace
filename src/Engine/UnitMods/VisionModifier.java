package Engine.UnitMods;

import Units.UnitContext;

public class VisionModifier implements UnitModifierWithDefaults
{
  private static final long serialVersionUID = 1L;
  private int sightMod = 0;
  private boolean pierce;

  public VisionModifier(int visionBoost)
  {
    this(visionBoost, visionBoost >= 0);
  }
  public VisionModifier(int visionBoost, boolean visionPierce)
  {
    sightMod = visionBoost;
    pierce = visionPierce;
  }

  @Override
  public void modifyVision(UnitContext uc)
  {
    uc.visionRange += sightMod;
    uc.visionPierces = pierce;
  }
}
