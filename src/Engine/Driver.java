package Engine;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

import Test.TestMain;
import UI.InputHandler;

public class Driver implements ActionListener, KeyListener
{
  private static final long serialVersionUID = 1L;

  JFrame gameWindow;
  private javax.swing.Timer repaintTimer;

  private MainController gameController;

  public Driver()
  {
    gameController = new MainController();

    gameWindow = new JFrame();
    gameWindow.add(gameController);
    gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    gameWindow.addKeyListener(this);
    gameWindow.pack();
    gameWindow.setVisible(true);

    // Draw the screen at (ideally) 60fps.
    repaintTimer = new javax.swing.Timer(16, this);
    repaintTimer.start();
  }

  @Override
  public void actionPerformed(ActionEvent arg0)
  {
    // Redraw the screen if needed.
    gameWindow.repaint();
  }

  @Override
  public void keyPressed(KeyEvent arg0)
  {
    boolean exitGame = false;

    InputHandler.InputAction action = InputHandler.pressKey(arg0);

    if( action != InputHandler.InputAction.NO_ACTION )
    {
      // Pass the action on to the main game controller. If it says it's done, close the program.
      exitGame = gameController.handleInput(action);
    }

    if(exitGame)
    {
      // We are done here. I would say clean up data or whatever, but this is Java.
      System.exit(0);
    }
  }

  @Override
  public void keyReleased(KeyEvent arg0)
  {
    InputHandler.releaseKey(arg0);
  }

  @Override
  public void keyTyped(KeyEvent arg0)
  {
    // Don't care.
  }

  public static void main(String args[])
  {
    // Run the test cases. If those all pass, launch the primary driver.
    if( !new TestMain().runTest() )
    {
      System.out.println("One or more tests failed!");
      System.exit(0);
    }

    new Driver();
  }
}
