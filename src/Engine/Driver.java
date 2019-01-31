package Engine;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import Terrain.Environment;
import Test.TestMain;
import UI.InputHandler;
import UI.MainUIController;
import UI.Art.SpriteArtist.SpriteEngine;

public class Driver implements ActionListener, KeyListener
{
  public static String baseUnitPath = "res/unit/";
  public static String baseFlipperUnitPath = "res/unit/flippers/";
  public static String baseFactionPath = "res/unit/factions/";
  
  private static Driver gameDriver;
  private IController gameController;
  private GameViewProxy gameView;
  private JFrame gameWindow;

  private javax.swing.Timer repaintTimer;

  private IController oldController = null;
  private IView oldView = null;

  public GraphicsEngine gameGraphics = null;

  private Driver()
  {
    Utils.paintAllFactions(baseUnitPath,baseFactionPath,false);
    Utils.paintAllFactions(baseFlipperUnitPath,baseFactionPath,true);
    
    // At game startup, we are at the main menu. Set up controller/viewer
    MainUIController mc = new MainUIController();
    gameGraphics = new SpriteEngine(); // Choose graphics engine based on config file, etc?
    gameView = new GameViewProxy(gameGraphics.getMainUIView(mc));
    gameController = mc;

    gameWindow = new JFrame();
    gameWindow.add(gameView);
    gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    gameWindow.addKeyListener(this);
    gameWindow.pack();
    gameWindow.setVisible(true);

    // Draw the screen at (ideally) 60fps.
    repaintTimer = new javax.swing.Timer(16, this);
    repaintTimer.start();

    gameView.init(); // This will enable the view proxy to start monitoring screen size changes.
  }

  /** Get the one and only Driver */
  public static Driver getInstance()
  {
    return gameDriver;
  }

  /**
   * Update the current state of the game to a new controller and game viewer.
   * The current controller/view are stored so that we can return to them once
   * the new controller determines it is completed.
   */
  // TODO: Should oldController/view be a stack?
  public void changeGameState(IController newControl, IView newView)
  {
    // Store off the old controller/view so we can return to them.
    oldView = gameView.view;
    oldController = gameController;

    gameController = newControl;
    // If the new View is valid (it may not be if we are returning to a
    // previously-stored state), then request it keep our screen dimensions.
    if( null != newView )
    {
      newView.setPreferredDimensions(gameView.getWidth(), gameView.getHeight());
      gameView.setView(newView);
      gameWindow.getContentPane().setSize(newView.getPreferredDimensions());
    }
    gameWindow.pack(); // Resize the window to match the new view's preferences.
  }

  public void updateView()
  {
    gameWindow.getContentPane().setSize(gameGraphics.getScreenDimensions());
    gameWindow.pack();
  }

  @Override // From ActionListener
  public void actionPerformed(ActionEvent arg0)
  {
    // Redraw the screen if needed.
    gameWindow.repaint();
  }

  public static void main(String args[])
  {
    // Run the test cases. If those all pass, launch the primary driver.
//    if( !new TestMain().runTest() )
//    {
//      System.out.println("One or more tests failed!");
//      System.exit(0);
//    }

    gameDriver = new Driver();
  }

  @Override // From KeyListener
  public void keyPressed(KeyEvent arg0)
  {
    InputHandler.InputAction action = InputHandler.pressKey(arg0);

    if( action != InputHandler.InputAction.NO_ACTION )
    {
      // Pass the action on to the main game controller; if it says it's done, switch to previous state.
      if(gameController.handleInput(action))
      {
        // Reinstate the previous controller/view.
        // If the top-level controller returns done, it's time to exit.
        changeGameState( oldController, oldView );

        // We've switched back to these, now make sure we don't do it again (forever).
        oldController = null;
        oldView = null;
      }
    }

    if( null == gameController )
    {
      // We are done here. I would say clean up data or whatever, but this is Java.
      System.exit(0);
    }
  }

  @Override // From KeyListener
  public void keyReleased(KeyEvent arg0)
  {
    InputHandler.releaseKey(arg0);
  }

  @Override // From KeyListener
  public void keyTyped(KeyEvent arg0)
  {
    // Don't care.
  }

  /**
   * GameViewProxy simply wraps the currently-active IView. This allows us to set a GameView into the
   * game's JFrame a single time and then not have to worry about managing components; we simply switch
   * out the renderer that the GameViewProxy/JPanel is using instead.
   */
  private static class GameViewProxy extends JPanel
  {
    private static final long serialVersionUID = 2373394816370307709L;

    private IView view;
    public GameViewProxy( IView v )
    {
      view = v;
    }

    public void init()
    {
      addComponentListener(new ComponentAdapter(){
        @Override
        public void componentResized(ComponentEvent evt)
        {
          view.setPreferredDimensions(evt.getComponent().getWidth(), evt.getComponent().getHeight());
        }
      });
    }

    public void setView( IView v )
    {
      view = v;
    }

    @Override // From JComponent
    public void paintComponent(Graphics g)
    {
      // Draw whatever should be drawn.
      view.render(g);
    }

    @Override // From JComponent
    public Dimension getPreferredSize()
    {
      return view.getPreferredDimensions();
    }
  }
}
