import java.awt.*;
import java.util.*;
import javax.swing.*;

class GamePanel extends JPanel {
    private static final int CELL_SIZE = 20;
    private Cell[][] grid;
    private int rows, cols;
    private Point playerPos;
    private Point end;
    private int moves = 0;
    private PathfindingGame game;
    private ArrayList<Point> visitedCells;
    private ArrayList<Point> traps;
    private ArrayList<Point> teleporters;
    private ArrayList<Point> keys;
    private ArrayList<Point> locks;
    private boolean hasKey = false;
    private int timeLimit;
    private int timeRemaining;
    private javax.swing.Timer timer;
    private Random random = new Random();

    public GamePanel(PathfindingGame game) {
        this.game = game;
        setBackground(Color.BLACK);
        visitedCells = new ArrayList<>();
        traps = new ArrayList<>();
        teleporters = new ArrayList<>();
        keys = new ArrayList<>();
        locks = new ArrayList<>();
        
        // Initialize timer
        timer = new javax.swing.Timer(1000, e -> {
            timeRemaining--;
            if (timeRemaining <= 0) {
                ((javax.swing.Timer)e.getSource()).stop();
                gameOver("Time's up!");
            }
            game.updateTimeLabel(timeRemaining);
        });
    }

    public void generateNewMaze(int level) {
        // Calculate grid size based on level
        rows = 15 + (level * 2);
        cols = 20 + (level * 2);
        grid = new Cell[rows][cols];
        visitedCells.clear();
        traps.clear();
        teleporters.clear();
        keys.clear();
        locks.clear();
        hasKey = false;
        
        // Set time limit based on level
        timeLimit = 60 + (level * 30);
        timeRemaining = timeLimit;
        timer.restart();
        
        // Initialize grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell();
                double baseDensity = 0.2 + (level * 0.015);
                double randomFactor = Math.sin(i * j * 0.1) * 0.1;
                if (Math.random() < baseDensity + randomFactor) {
                    grid[i][j].isWall = true;
                }
            }
        }
        
        // Set start and end points
        playerPos = new Point(0, 0);
        end = new Point(cols-1, rows-1);
        grid[playerPos.y][playerPos.x].isWall = false;
        grid[end.y][end.x].isWall = false;
        
        // Add special elements
        addSpecialElements(level);
        
        ensurePathExists();
        visitedCells.add(new Point(playerPos.x, playerPos.y));
        repaint();
    }

    private void addSpecialElements(int level) {
        // Add traps
        int numTraps = level * 3;
        for (int i = 0; i < numTraps; i++) {
            addRandomElement(traps);
        }
        
        // Add teleporters
        int numTeleporterPairs = level / 2 + 1;
        for (int i = 0; i < numTeleporterPairs; i++) {
            addRandomElement(teleporters);
            addRandomElement(teleporters);
        }
        
        // Add keys and locks
        int numKeyLockPairs = level / 3 + 1;
        for (int i = 0; i < numKeyLockPairs; i++) {
            addRandomElement(keys);
            addRandomElement(locks);
        }
    }

    private void addRandomElement(ArrayList<Point> elements) {
        int x, y;
        do {
            x = random.nextInt(cols);
            y = random.nextInt(rows);
        } while (grid[y][x].isWall || isSpecialPoint(x, y));
        elements.add(new Point(x, y));
    }

    private boolean isSpecialPoint(int x, int y) {
        Point p = new Point(x, y);
        return p.equals(playerPos) || p.equals(end) || 
               traps.contains(p) || teleporters.contains(p) ||
               keys.contains(p) || locks.contains(p);
    }

    public void movePlayer(int dx, int dy) {
        int newX = playerPos.x + dx;
        int newY = playerPos.y + dy;
        
        if (isValidMove(newX, newY)) {
            Point newPos = new Point(newX, newY);
            
            if (handleSpecialTiles(newPos)) {
                playerPos.x = newX;
                playerPos.y = newY;
                moves++;
                visitedCells.add(new Point(newX, newY));
                
                checkLevelCompletion();
                repaint();
            }
        }
    }

    private boolean handleSpecialTiles(Point newPos) {
        // Check for traps
        if (traps.contains(newPos)) {
            gameOver("You fell into a trap!");
            return false;
        }
        
        // Check for teleporters
        if (teleporters.contains(newPos)) {
            handleTeleporter(newPos);
            return true;
        }
        
        // Check for keys
        if (keys.contains(newPos)) {
            hasKey = true;
            keys.remove(newPos);
        }
        
        // Check for locks
        if (locks.contains(newPos)) {
            if (!hasKey) return false;
            hasKey = false;
            locks.remove(newPos);
        }
        
        return true;
    }

    private void handleTeleporter(Point pos) {
        int index = teleporters.indexOf(pos);
        int targetIndex = (index % 2 == 0) ? index + 1 : index - 1;
        if (targetIndex >= 0 && targetIndex < teleporters.size()) {
            Point target = teleporters.get(targetIndex);
            playerPos.x = target.x;
            playerPos.y = target.y;
        }
    }

    private void checkLevelCompletion() {
        if (playerPos.x == end.x && playerPos.y == end.y && locks.isEmpty()) {
            timer.stop();
            game.levelCompleted(timeRemaining);
        }
    }

    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < cols && 
               y >= 0 && y < rows && 
               !grid[y][x].isWall;
    }

    private void gameOver(String message) {
        timer.stop();
        JOptionPane.showMessageDialog(this, message + "\nGame Over at Level " + game.getCurrentLevel());
        game.restartGame();
    }

    public int getMoves() {
        return moves;
    }

    private void ensurePathExists() {
        boolean[][] visited = new boolean[rows][cols];
        Stack<Point> stack = new Stack<>();
        stack.push(playerPos);
        
        while (!stack.empty()) {
            Point current = stack.pop();
            if (current.x == end.x && current.y == end.y) {
                return; // Path found
            }
            
            if (!visited[current.y][current.x]) {
                visited[current.y][current.x] = true;
                
                int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
                for (int[] dir : dirs) {
                    int newX = current.x + dir[0];
                    int newY = current.y + dir[1];
                    
                    if (isValidMove(newX, newY) && !visited[newY][newX]) {
                        grid[newY][newX].isWall = false;
                        stack.push(new Point(newX, newY));
                    }
                }
            }
        }
        
        generateNewMaze(1); // If no path found, regenerate maze
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Calculate scaling factors
        double scaleX = getWidth() / (double)(cols * CELL_SIZE);
        double scaleY = getHeight() / (double)(rows * CELL_SIZE);
        g2d.scale(scaleX, scaleY);
        
        // Draw all components
        drawGrid(g2d);
        drawVisitedCells(g2d);
        drawSpecialElements(g2d);
        drawPlayer(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j].isWall) {
                    g2d.setColor(Color.GRAY);
                    g2d.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private void drawVisitedCells(Graphics2D g2d) {
        g2d.setColor(new Color(100, 100, 255, 50));
        for (Point p : visitedCells) {
            g2d.fillRect(p.x * CELL_SIZE, p.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }
    }

    private void drawSpecialElements(Graphics2D g2d) {
        // Draw end point
        g2d.setColor(Color.RED);
        g2d.fillRect(end.x * CELL_SIZE, end.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        
        // Draw traps
        g2d.setColor(Color.RED);
        for (Point trap : traps) {
            g2d.fillOval(trap.x * CELL_SIZE + 2, trap.y * CELL_SIZE + 2, 
                        CELL_SIZE - 4, CELL_SIZE - 4);
        }
        
        // Draw teleporters
        g2d.setColor(Color.CYAN);
        for (int i = 0; i < teleporters.size(); i += 2) {
            Point t1 = teleporters.get(i);
            Point t2 = i + 1 < teleporters.size() ? teleporters.get(i + 1) : t1;
            drawTeleporter(g2d, t1.x * CELL_SIZE, t1.y * CELL_SIZE);
            drawTeleporter(g2d, t2.x * CELL_SIZE, t2.y * CELL_SIZE);
        }
        
        // Draw keys and locks
        g2d.setColor(Color.YELLOW);
        for (Point key : keys) {
            drawKey(g2d, key.x * CELL_SIZE, key.y * CELL_SIZE);
        }
        
        g2d.setColor(hasKey ? Color.GREEN : Color.RED);
        for (Point lock : locks) {
            drawLock(g2d, lock.x * CELL_SIZE, lock.y * CELL_SIZE);
        }
    }

    private void drawPlayer(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);
        g2d.fillOval(playerPos.x * CELL_SIZE + 2, playerPos.y * CELL_SIZE + 2, 
                     CELL_SIZE - 4, CELL_SIZE - 4);
    }

    private void drawTeleporter(Graphics2D g2d, int x, int y) {
        g2d.drawOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        g2d.drawOval(x + 4, y + 4, CELL_SIZE - 8, CELL_SIZE - 8);
    }

    private void drawKey(Graphics2D g2d, int x, int y) {
        g2d.fillOval(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        g2d.fillRect(x + CELL_SIZE/2, y + CELL_SIZE/2, CELL_SIZE/2 - 5, CELL_SIZE/4);
    }

    private void drawLock(Graphics2D g2d, int x, int y) {
        g2d.fillRect(x + 5, y + CELL_SIZE/2, CELL_SIZE - 10, CELL_SIZE/2 - 5);
        g2d.drawArc(x + 5, y + 5, CELL_SIZE - 10, CELL_SIZE/2, 0, 180);
    }
}