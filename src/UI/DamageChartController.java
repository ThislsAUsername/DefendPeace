package UI;



import java.awt.Color;

import CommandingOfficers.Commander;
import Engine.IController;
import Engine.OptionSelector;
import UI.InputHandler.InputAction;
import UI.UIUtils.Faction;
import Units.UnitModel;

public class DamageChartController implements IController
{
  OptionSelector targetSelector;
  OptionSelector shooterSelector;
  public final Faction shooters, targets;
  public final Color shooterColor, targetColor;
  public final UnitModel[] units;
  public boolean outOfAmmo = false;

  public DamageChartController(Commander attacker, Commander defender)
  {
    // TODO: Account for variable unit model count?
    units = attacker.unitModels.toArray(new UnitModel[0]);
    shooters = attacker.faction;
    targets = defender.faction;
    shooterColor = attacker.myColor;
    targetColor = defender.myColor;
    targetSelector = new OptionSelector(units.length);
    shooterSelector = new OptionSelector(units.length);
  }
  public DamageChartController(UnitModel[] models, Faction attackers, Faction defenders, Color attackerColor, Color defenderColor)
  {
    units = models;
    shooters = attackers;
    targets = defenders;
    shooterColor = attackerColor;
    targetColor = defenderColor;
    targetSelector = new OptionSelector(units.length);
    shooterSelector = new OptionSelector(units.length);
  }

  @Override
  public boolean handleInput(InputAction action)
  {
    boolean done = false;
    switch(action)
    {
      case SEEK:
        outOfAmmo = !outOfAmmo;
        break;
      case SELECT:
        // TODO: Add a fancy control to do detailed damage calc stuff here
        done = true;
        break;
      case UP:
      case DOWN:
        targetSelector.handleInput(action);
        break;
      case LEFT:
      case RIGHT:
        shooterSelector.handleInput(action);
        break;
      case BACK:
        done = true;
        break;
      default:
        // Do nothing.
    }
    return done;
  }

  public int getSelectedShooter()
  {
    return shooterSelector.getSelectionNormalized();
  }

  public int getSelectedTarget()
  {
    return targetSelector.getSelectionNormalized();
  }
}
