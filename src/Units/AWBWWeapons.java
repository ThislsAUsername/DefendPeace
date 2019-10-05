package Units;

public class AWBWWeapons
{
  public static class InfantryMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public InfantryMGun()
    {
      super(WeaponType.INFANTRYMGUN);
    }
  }

  public static class MechMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public MechMGun()
    {
      super(WeaponType.MECHMGUN);
    }
  }

  public static class MechZooka extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 3;

    public MechZooka()
    {
      super(WeaponType.MECHZOOKA, MAX_AMMO);
    }
  }

  public static class ReconMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public ReconMGun()
    {
      super(WeaponType.RECONMGUN);
    }
  }

  public static class TankMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public TankMGun()
    {
      super(WeaponType.TANKMGUN);
    }
  }

  public static class TankCannon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public TankCannon()
    {
      super(WeaponType.TANKCANNON, MAX_AMMO);
    }
  }

  public static class MDTankMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public MDTankMGun()
    {
      super(WeaponType.MD_TANKMGUN);
    }
  }

  public static class MDTankCannon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 8;

    public MDTankCannon()
    {
      super(WeaponType.MD_TANKCANNON, MAX_AMMO);
    }
  }

  public static class NeoMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public NeoMGun()
    {
      super(WeaponType.NEOMGUN);
    }
  }

  public static class NeoCannon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public NeoCannon()
    {
      super(WeaponType.NEOCANNON, MAX_AMMO);
    }
  }

  public static class MegaMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public MegaMGun()
    {
      super(WeaponType.MEGAMGUN);
    }
  }

  public static class MegaCannon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 3;

    public MegaCannon()
    {
      super(WeaponType.MEGACANNON, MAX_AMMO);
    }
  }

  public static class ArtilleryCannon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 3;

    public ArtilleryCannon()
    {
      super(WeaponType.ARTILLERYCANNON, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class RocketRockets extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public RocketRockets()
    {
      super(WeaponType.ROCKETS, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class PipeGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 5;

    public PipeGun()
    {
      super(WeaponType.PIPEGUN, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class AntiAirMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public AntiAirMGun()
    {
      super(WeaponType.ANTI_AIRMGUN, MAX_AMMO);
    }
  }

  public static class MobileSAMWeapon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 5;

    public MobileSAMWeapon()
    {
      super(WeaponType.MOBILESAM, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  // air

  public static class CopterMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public CopterMGun()
    {
      super(WeaponType.B_COPTERMGUN);
    }
  }

  public static class CopterRockets extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 6;

    public CopterRockets()
    {
      super(WeaponType.B_COPTERROCKETS, MAX_AMMO);
    }
  }

  public static class BomberBombs extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public BomberBombs()
    {
      super(WeaponType.BOMBERBOMBS, MAX_AMMO);
    }
  }

  public static class FighterMissiles extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public FighterMissiles()
    {
      super(WeaponType.FIGHTERMISSILES, MAX_AMMO);
    }
  }

  public static class StealthShots extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 6;

    public StealthShots()
    {
      super(WeaponType.STEALTH_SHOTS, MAX_AMMO);
    }
  }

  // sea

  public static class SubTorpedoes extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 6;

    public SubTorpedoes()
    {
      super(WeaponType.SUBTORPEDOES, MAX_AMMO);
    }
  }

  public static class BattleshipCannon extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 2;
    private static final int MAX_RANGE = 6;

    public BattleshipCannon()
    {
      super(WeaponType.BATTLESHIPCANNON, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class CarrierMissiles extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;
    private static final int MIN_RANGE = 3;
    private static final int MAX_RANGE = 8;

    public CarrierMissiles()
    {
      super(WeaponType.CARRIERMISSILES, MAX_AMMO, MIN_RANGE, MAX_RANGE);
    }
  }

  public static class CruiserMGun extends WeaponModel
  {
    private static final long serialVersionUID = 1L;

    public CruiserMGun()
    {
      super(WeaponType.CRUISERMGUN);
    }
  }

  public static class CruiserTorpedoes extends WeaponModel
  {
    private static final long serialVersionUID = 1L;
    private static final int MAX_AMMO = 9;

    public CruiserTorpedoes()
    {
      super(WeaponType.CRUISERTORPEDOES, MAX_AMMO);
    }
  }

}
