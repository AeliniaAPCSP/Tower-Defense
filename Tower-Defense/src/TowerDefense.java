import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class TowerDefense extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'R'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            //char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            /*
            for (Block road : roads) {
                if (collision(this, road)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
            */
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/16;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/16;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/16;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/16;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 12;
    private int columnCount = 18;
    private int tileSize = 64;
    private int boardWidth = columnCount * tileSize - tileSize * 2;
    private int boardHeight = rowCount * tileSize - tileSize * 2;

    private Image roadImage;
    private Image towerImage;
    private Image enemy1Image;

    //R = road, T = tower, ' ' = ground
    private String[] tileMap = {
            "          R ",
            "    T     R ",
            "  RRRRR T RT",
            "  R   RRRRR ",
            " TR T   T   ",
            "  R   RRRRR ",
            "  RRRRR   R ",
            "    T   T RT",
            "1RRRRRR   R ",
            "    T RRRRR ",
            "            "
    };

    HashSet<Block> roads;
    HashSet<Block> grounds;
    HashSet<Block> towers;
    Block enemyTest;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    /*
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;
    */
    TowerDefense() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(new Color(34, 177, 76));
        addKeyListener(this);
        setFocusable(true);

        //load images
        roadImage = new ImageIcon(getClass().getResource("./assets/road.png")).getImage();
        towerImage = new ImageIcon(getClass().getResource("./assets/tower.png")).getImage();
        enemy1Image = new ImageIcon(getClass().getResource("./assets/enemy1.png")).getImage();
        loadMap();

        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        roads = new HashSet<Block>();
        grounds = new HashSet<Block>();
        towers = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {

                int x = c * tileSize - tileSize / 2;
                int y = r * tileSize - tileSize / 2;

                if(r < rowCount - 1 && c < columnCount - 6) {
                    String row = tileMap[r];
                    char tileMapChar = row.charAt(c);
                    if (tileMapChar == 'R') { //block road
                        Block road = new Block(roadImage, x, y, tileSize, tileSize);
                        roads.add(road);
                    }
                    else if (tileMapChar == 'T') { //towers
                        Block tower = new Block(towerImage, x, y, tileSize, tileSize);
                        towers.add(tower);
                    }
                    else if (tileMapChar == ' ') { //ground
                        Block ground = new Block(null, x, y, 8, 8);
                        grounds.add(ground);
                    }
                    else if (tileMapChar == '1') { //enemy1
                        enemyTest = new Block(enemy1Image, x, y, tileSize, tileSize);
                    }
                } else {
                    Block ground = new Block(null, x, y, 8, 8);
                    grounds.add(ground);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        for (Block road : roads) {
            g.drawImage(road.image, road.x, road.y, road.width, road.height, null);
        }

        for (Block tower : towers) {
            g.drawImage(tower.image, tower.x, tower.y, tower.width, tower.height, null);
        }

        g.drawImage(new ImageIcon(getClass().getResource("./assets/towerPanel.png")).getImage(), tileSize * 12, 0, tileSize * 4, tileSize * 10, null);
        g.drawImage(enemy1Image, enemyTest.x, enemyTest.y, enemyTest.width, enemyTest.height, null);
        //score
        /*
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        }
        */
    }

    public void move() {
        enemyTest.x += enemyTest.velocityX;
        enemyTest.y += enemyTest.velocityY;

        //check wall collisions
        /*
        for (Block ground : grounds) {
            if (collision(enemyTest, ground)) {
                enemyTest.x -= enemyTest.velocityX;
                enemyTest.y -= enemyTest.velocityY;
                break;
            }
        }
        */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        /*
        if (gameOver) {
            gameLoop.stop();
        }
        */
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {

        System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            enemyTest.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            enemyTest.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            enemyTest.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            enemyTest.updateDirection('R');
        }

    }
}