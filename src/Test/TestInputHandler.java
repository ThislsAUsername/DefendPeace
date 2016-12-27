package Test;

import java.awt.Component;
import java.awt.event.KeyEvent;

import UI.InputHandler;

public class TestInputHandler extends TestCase
{
  @Override
  public boolean runTest()
  {
    boolean testPassed = true;

    testPassed &= validate(!InputHandler.isUpHeld(), "  Up should not be held at start");
    testPassed &= validate(!InputHandler.isDownHeld(), "  Down should not be held at start");
    testPassed &= validate(!InputHandler.isLeftHeld(), "  Left should not be held at start");
    testPassed &= validate(!InputHandler.isRightHeld(), "  Right should not be held at start");

    // Build a KeyEvent to simulate pushing UP, and confirm InputHandler treats this as an up action.
    KeyEvent upKey = new KeyEvent(new DummyComponent(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED);
    InputHandler.InputAction action = InputHandler.pressKey(upKey);
    testPassed &= validate(action == InputHandler.InputAction.UP, "  Up key was not correctly mapped to UP action.");

    // A single press means that InputHandler should not record this key as being held down.
    testPassed &= validate(!InputHandler.isUpHeld(), "  Up should not be reported as held");

    // Call pressKey again. Two presses in a row should cause the key to be treated as held.
    InputHandler.pressKey(upKey);
    testPassed &= validate(InputHandler.isUpHeld(), "  Up should be reported as held");

    // Let go of the key and verify.
    InputHandler.releaseKey(upKey);
    testPassed &= validate(!InputHandler.isUpHeld(), "  InputHandler did not register up key release");

    return testPassed;
  }

  private class DummyComponent extends Component
  {
    private static final long serialVersionUID = 1L;
  }
}
