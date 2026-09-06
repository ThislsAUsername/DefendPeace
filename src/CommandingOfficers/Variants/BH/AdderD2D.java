package CommandingOfficers.Variants.BH;

import CommandingOfficers.*;
import CommandingOfficers.AWBW.AWBWCommander;
import Engine.GameScenario;
import UI.UIUtils;
import Units.UnitContext;

public class AdderD2D extends AWBWCommander
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
      super("Adder", UIUtils.SourceGames.VARIANTS, UIUtils.BH, "D2D");
      infoPages.add(new InfoPage(
            "Adder (D2D)\n"
          + "+1 move.\n"
          + "0.9x capture power if he uses his extra movepoint.\n"));
      infoPages.add(new InfoPage(
            "Hit: His own face\n"
          + "Miss: Dirty things"));
      infoPages.add(AWBW_MECHANICS_BLURB);
    }
    @Override
    public Commander create(GameScenario.GameRules rules)
    {
      return new AdderD2D(rules);
    }
  }

  public AdderD2D(GameScenario.GameRules rules)
  {
    super(coInfo, rules);
  }

  @Override
  public void modifyMovePower(UnitContext uc)
  {
    uc.movePower += 1;
  }
  @Override
  public void modifyCapturePower(UnitContext uc)
  {
    if( null == uc.path || uc.path.getFuelCost(uc.unit, army.myView) > uc.model.baseMovePower )
      uc.capturePower -= 10;
  }

}
