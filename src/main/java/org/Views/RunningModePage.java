package org.Views;

import org.Domain.*;
import org.Controllers.*;
import org.Utils.Database;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import org.bson.Document;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class RunningModePage extends Page{
    private BufferedImage backgroundImage;
    private JPanel gamePanel =  new JPanel();
    private JPanel infoContainer =  new JPanel();
    protected RunningModeController runningModeController;
    public boolean pause = false;
    private ArrayList<Barrier> barriers = new ArrayList<>();
    private ArrayList<Debris> activeDebris;
    private ArrayList<Spell> droppingSpells;
    private ArrayList<Bullet> activeBullets;
    public static final int SCREENWIDTH =1000;
    public int screenHeight;
    public int timeInSeconds = 0;
    private int frameCount = 0;
    private Timer gameTimer =  new Timer();
    private Sound sound=new Sound();

    public static final long COLLISION_COOLDOWN = 1000; // Cooldown period in milliseconds

    private JLabel timeLabel;
    public RunningModePage() {
        super();
        activeDebris = new ArrayList<>();
        droppingSpells = new ArrayList<>();
        activeBullets=new ArrayList<>();
        this.setDoubleBuffered(true);
        try {
            backgroundImage = ImageIO.read(new File("assets/Background.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.runningModeController = new RunningModeController(this);
        this.runningModeController.getGameSession().started = true;
        initUI();
        setFocusable(true);
        requestFocus();
        setupTimer();
    }
    public ArrayList<Debris> getActiveDebris() {  return activeDebris;   }
    public ArrayList<Spell> getDroppingSpells() { return droppingSpells; }
    public ArrayList<Bullet> getActiveBullets(){ return activeBullets;}


    protected void paintComponent(Graphics g) { //background for the whole frame
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }
    private void setupTimer() {
        int delay = 0;  // start immediately
        int period = 16; // 16 ms period for approx. 60 FPS

        TimerTask task = new TimerTask() {
            public void run() {
                // For Pausing the game
                if (pause) {
                    //Object[] options = {"Continue", "Quit", "Save"};
                    Object[] options = {"Continue", "Quit"};
                    int choice = JOptionPane.showOptionDialog(null,
                            "You paused",
                            "Game Paused",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[0]);
                    if (choice == JOptionPane.YES_OPTION) {
                        pause = false;
                        gamePanel.requestFocus();
                    } else if (choice == JOptionPane.NO_OPTION) {
                        pause = false;
                        runningModeController = null;
                        Navigator.getInstance().showStartSingleplayerPage();
                    }
                }
                else if (runningModeController.getGameSession().ended) {
                    JOptionPane.showMessageDialog(null, "You lost!");
                    runningModeController = null;
                    Navigator.getInstance().showStartSingleplayerPage();
                }
                else {
                    // Update the game frame
                    SwingUtilities.invokeLater(() -> updateGameFrame());
                    // Manage time and frames
                    frameCount++;
                    if (frameCount >= 70) {
                        timeInSeconds++;
                        SwingUtilities.invokeLater(() -> timeLabel.setText("Time: " + timeInSeconds + "s"));
                        frameCount = 0;
                    }
                }
            }
        };
        gameTimer.scheduleAtFixedRate(task, delay, period);
    }

    public void updateGameFrame() {
        runningModeController.updateFireballView();
        runningModeController.updateMagicalStaffView();
        runningModeController.checkMagicalStaffFireballCollision();
        runningModeController.checkScreenBordersFireballCollision();
        runningModeController.checkBarrierFireballCollision();
        runningModeController.moveBarriers();
        runningModeController.updateDebris();// Handle debris movement
        runningModeController.updateDroppingSpells();// Hande spell dropping
        runningModeController.updateHexBullets();
        repaint();
        if (this.runningModeController.getGameSession().getChance().getRemainingChance() == 0) {
            this.runningModeController.getGameSession().ended = true;


        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                    runningModeController.run();
                    repaint();
            }
        });
    }

    public JPanel getGamePanel() {
        return this.gamePanel;
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout());
        initializeGameObjects();
        playMusic(0);

    }

    private void initializeGameObjects() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // infoContainer is the container that displays chance and score information.
                String hexCode = "#FFFFFF";
                Color color = Color.decode(hexCode);
                infoContainer = new JPanel(new FlowLayout());
                infoContainer.setPreferredSize(new Dimension(190, 500));
                infoContainer.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));

                timeLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
                infoContainer.add(timeLabel);

                JButton pauseButton = new JButton("Pause");
                pauseButton.addActionListener(e -> pause = true);

// Add the pause button to the info container
                infoContainer.add(pauseButton);

                // Adding infoContainer the chance and score instances which are already visual JPanels.
                infoContainer.add(runningModeController.getGameSession().getChance());
                infoContainer.add(runningModeController.getGameSession().getScore());

                add(infoContainer, BorderLayout.WEST);

                JLabel statusLabel = new JLabel("Running Mode", SwingConstants.CENTER);
                add(statusLabel, BorderLayout.SOUTH);
                statusLabel.setBackground(Color.lightGray); // Set background color
                statusLabel.setOpaque(true);

                gamePanel= new JPanel();
                gamePanel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)); // Here, 128 represents the alpha value (semi-transparent)
                gamePanel.setLayout(null);
                gamePanel.addKeyListener(new MyKeyListener(runningModeController));
                gamePanel.setFocusable(true);
                gamePanel.requestFocus();
                gamePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        System.out.println("Mouse click coordinates:"+ e.getX()+" "+ e.getY());
                    }
                });

                Border border = BorderFactory.createLineBorder(Color.BLACK);
                gamePanel.setPreferredSize(new Dimension(1000, 500)); // Set preferred size of buildingPanel
                gamePanel.addNotify();
                //gamePanel.revalidate();
                gamePanel.setBorder(border);
                add(gamePanel, BorderLayout.EAST);
                //screenWidth = 1000; assigned at top and finalized
                //System.out.println("screenWidth"+ screenWidth);
                screenHeight = 500;

                // Initializing Fireball
                int fireballWidth = runningModeController.getGameSession().getFireball().getPreferredSize().width;
                int fireballPositionX = (SCREENWIDTH - fireballWidth) / 2;
                int fireballHeight = runningModeController.getGameSession().getFireball().getPreferredSize().height;
                int fireballPositionY = (screenHeight - fireballHeight - 200);
                runningModeController.getGameSession().getFireball().getCoordinate().setX(fireballPositionX);
                runningModeController.getGameSession().getFireball().getCoordinate().setY(fireballPositionY);
                runningModeController.getGameSession().getFireball().setBounds(fireballPositionX, fireballPositionY, fireballWidth, fireballHeight);
                gamePanel.add(runningModeController.getGameSession().getFireball());

                // Initializing MagicalStaff
                int magicalStaffWidth = runningModeController.getGameSession().getMagicalStaff().getPreferredSize().width;
                int magicalStaffHeight = runningModeController.getGameSession().getMagicalStaff().getPreferredSize().height;
                int magicalStaffPositionX = runningModeController.getGameSession().getMagicalStaff().getCoordinate().getX();
                int magicalStaffPositionY = runningModeController.getGameSession().getMagicalStaff().getCoordinate().getY();
                runningModeController.getGameSession().getMagicalStaff().setBounds(magicalStaffPositionX, magicalStaffPositionY, magicalStaffWidth, magicalStaffHeight);

                gamePanel.requestFocus();
                gamePanel.setFocusTraversalKeysEnabled(false);
                gamePanel.add(runningModeController.getGameSession().getMagicalStaff());

                //to follow if view has focus:
                gamePanel.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        System.out.println("Game panel has gained focus");
                    }
                    @Override
                    public void focusLost(FocusEvent e) {
                        System.out.println("Game panel has lost focus");
                    }
                });

                // Initialize Barriers
                barriers = runningModeController.getGameSession().getBarriers();
                for (Barrier barrier : barriers) {
                    barrier.setBounds(barrier.getCoordinate().getX(), barrier.getCoordinate().getY(), barrier.getPreferredSize().width, barrier.getPreferredSize().height);
                    barrier.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
                    gamePanel.add(barrier);
                }
            }
        });
    }

    private void saveGame() {
        String gameName = JOptionPane.showInputDialog(this, "Enter a name for your save file:", "Save Game", JOptionPane.PLAIN_MESSAGE);
        if (gameName == null || gameName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please provide a name to save the game.");
            return;
        }
        int timeElapsed=timeInSeconds;
        runningModeController.saveGame(gameName,timeElapsed, activeDebris);
    }
    public void playMusic(int i){
        sound.setFile(i);
        sound.play();
        sound.loop();
    }
    public void stopMusic(){
        sound.stop();
    }

    public void playSoundEffect(int i){
        sound.setFile(i);
        sound.play();
    }
}