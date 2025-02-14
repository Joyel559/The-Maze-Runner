import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PathfindingGame extends JFrame {
    private GamePanel gamePanel;
    private int currentLevel = 1;
    private JLabel levelLabel;
    private JLabel movesLabel;
    private JLabel timeLabel;
    private JLabel scoreLabel;
    private int totalScore = 0;

    public PathfindingGame() {
        setTitle("Maze Pathfinding Game - Level " + currentLevel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Initialize components
        gamePanel = new GamePanel(this);
        levelLabel = new JLabel("Level: " + currentLevel);
        movesLabel = new JLabel("Moves: 0");
        timeLabel = new JLabel("Time: 60");
        scoreLabel = new JLabel("Score: 0");
        
        // Style labels
        Font labelFont = new Font("Arial", Font.BOLD, 14);
        for (JLabel label : new JLabel[]{levelLabel, movesLabel, timeLabel, scoreLabel}) {
            label.setFont(labelFont);
            label.setForeground(Color.WHITE);
        }
        
        // Create info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.DARK_GRAY);
        infoPanel.add(levelLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(movesLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(timeLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(scoreLabel);
        
        add(gamePanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);
        
        // Add keyboard listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:    gamePanel.movePlayer(0, -1); break;
                    case KeyEvent.VK_DOWN:  gamePanel.movePlayer(0, 1); break;
                    case KeyEvent.VK_LEFT:  gamePanel.movePlayer(-1, 0); break;
                    case KeyEvent.VK_RIGHT: gamePanel.movePlayer(1, 0); break;
                    case KeyEvent.VK_R:     restartGame(); break;
                }
                movesLabel.setText("Moves: " + gamePanel.getMoves());
                requestFocus();
            }
        });
        
        setFocusable(true);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        gamePanel.generateNewMaze(currentLevel);
        requestFocus();
    }

    public void updateTimeLabel(int seconds) {
        timeLabel.setText("Time: " + seconds);
    }

    public void levelCompleted(int timeRemaining) {
        int levelScore = calculateScore(timeRemaining);
        totalScore += levelScore;
        
        String message = String.format(
            "Level %d Completed!\n" +
            "Time Bonus: %d\n" +
            "Level Score: %d\n" +
            "Total Score: %d",
            currentLevel, timeRemaining, levelScore, totalScore
        );
        
        JOptionPane.showMessageDialog(this, message);
        
        currentLevel++;
        scoreLabel.setText("Score: " + totalScore);
        levelLabel.setText("Level: " + currentLevel);
        setTitle("Maze Pathfinding Game - Level " + currentLevel);
        
        gamePanel.generateNewMaze(currentLevel);
    }

    private int calculateScore(int timeRemaining) {
        return (currentLevel * 100) + (timeRemaining * 10);
    }

    public void restartGame() {
        currentLevel = 1;
        totalScore = 0;
        levelLabel.setText("Level: " + currentLevel);
        scoreLabel.setText("Score: 0");
        setTitle("Maze Pathfinding Game - Level " + currentLevel);
        gamePanel.generateNewMaze(currentLevel);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new PathfindingGame().setVisible(true);
        });
    }
}