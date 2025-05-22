import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class TowerDefense extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;
        String type;

        char direction = 'R'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height, String type) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        void updateDirection(char direction) {
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
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
    }

    class Enemy extends Block {
        int health;
        int priority = 0;
        double speed;

        Enemy(Image image, int x, int y, int width, int height, String type, int health, double speed) {
            super(image, x, y, width, height, type);
            this.health = health;
            this.speed = speed;
        }
    }

    class Tower extends Block {
        int damage;
        int range;
        int fireRate;

        Tower(Image image, int x, int y, int width, int height, String type, int damage, int fireRate, int range) {
            super(image, x, y, width, height, type);
            this.damage = damage;
            this.fireRate = fireRate;
            this.range = range;
        }
    }

    private int rowCount = 12;
    private int columnCount = 18;
    private int tileSize = 64;
    private int boardWidth = columnCount * tileSize - tileSize * 2;
    private int boardHeight = rowCount * tileSize - tileSize * 2;

    private int animationCounter = 0;

    private Image roadImage;
    private Image tower1Image;
    private Image tower2Image;
    private Image enemy1Image;
    private Image selectorImage1;
    private Image selectorImage2;

    //# = road, L = left turn, R = right turn, T = tower, ' ' = ground
    private String[] tileMap = {
            "          # ",
            "          # ",
            "  R###R   # ",
            "  #   L###L ",
            "  #         ",
            "  #   L###L ",
            "  R###R   # ",
            "          # ",
            "1#####R   # ",
            "      L###L ",
            "            "
    };

    ArrayList<Block> tiles = new ArrayList<Block>();
    ArrayList<Tower> towers = new ArrayList<Tower>();
    ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    Block selector;

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
        tower1Image = new ImageIcon(getClass().getResource("./assets/tower1.png")).getImage();
        tower2Image = new ImageIcon(getClass().getResource("./assets/tower2.png")).getImage();
        enemy1Image = new ImageIcon(getClass().getResource("./assets/enemy1.png")).getImage();
        selectorImage1 = new ImageIcon(getClass().getResource("./assets/selector1.png")).getImage();
        selectorImage2 = new ImageIcon(getClass().getResource("./assets/selector2.png")).getImage();
        loadMap();

        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {

                int x = c * tileSize - tileSize / 2;
                int y = r * tileSize - tileSize / 2;

                if(r < rowCount - 1 && c < columnCount - 6) {
                    String row = tileMap[r];
                    char tileMapChar = row.charAt(c);
                    if (tileMapChar == '#') { //block road
                        Block road = new Block(roadImage, x, y, tileSize, tileSize, "road");
                        tiles.add(road);
                    } else if (tileMapChar == 'L') { //block road
                        Block road = new Block(roadImage, x, y, tileSize, tileSize, "left");
                        tiles.add(road);
                    } else if (tileMapChar == 'R') { //block road
                        Block road = new Block(roadImage, x, y, tileSize, tileSize, "right");
                        tiles.add(road);
                    } else if (tileMapChar == ' ') { //ground
                        Block ground = new Block(null, x, y, 8, 8, "ground");
                        tiles.add(ground);
                    } else if (tileMapChar == '1') { //enemy1
                        Block ground = new Block(null, x, y, 8, 8, "ground");
                        tiles.add(ground);
                        Enemy enemy1 = new Enemy(enemy1Image, x, y, tileSize, tileSize, "enemy1", 100, 1);
                        enemies.add(enemy1);
                    }
                } else {
                    Block ground = new Block(null, x, y, 8, 8, "ground");
                    tiles.add(ground);
                }
            }
        }
        selector = new Block(selectorImage1, tileSize*3/2, tileSize*3/2, tileSize, tileSize, "selector");
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        for (Block road : tiles) {
            g.drawImage(road.image, road.x, road.y, road.width, road.height, null);
        }
        for (Block tower : towers) {
            g.drawImage(tower.image, tower.x, tower.y, tower.width, tower.height, null);
        }
        for (Block enemy : enemies) {
            g.drawImage(enemy.image, enemy.x, enemy.y, enemy.width, enemy.height, null);
        }

        g.drawImage(new ImageIcon(getClass().getResource("./assets/towerPanel.png")).getImage(), tileSize * 12, 0, tileSize * 4, tileSize * 10, null);
        
        if (animationCounter > 7) {
            g.drawImage(selectorImage1, selector.x, selector.y, selector.width, selector.height, null);
        } else {
            g.drawImage(selectorImage2, selector.x, selector.y, selector.width, selector.height, null);
        }
        animationCounter++;
        if (animationCounter == 16) {
            animationCounter = 0;
        }
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

    public boolean isTileOnType(Block tile, String type) {
        return tiles.get((tile.x - tileSize/2) / tileSize + ((tile.y - tileSize/2) / tileSize) * (columnCount - 6)).type.equals(type);
    }

    public void move() {
        for(Block enemy : enemies) {
            if(enemy.velocityX == 0 && enemy.velocityY == 0) {
                enemy.velocityX = tileSize/16;
            }
            enemy.x += enemy.velocityX;
            enemy.y += enemy.velocityY;
            if (isTileOnType(enemy, "left")) {
                if (enemy.direction == 'U') {
                    enemy.updateDirection('L');
                } else if (enemy.direction == 'D') {
                    enemy.updateDirection('R');
                } else if (enemy.direction == 'L') {
                    enemy.updateDirection('D');
                } else if (enemy.direction == 'R') {
                    enemy.updateDirection('U');
                }
                break;
            } else if (isTileOnType(enemy, "left")) {
                if (enemy.direction == 'U') {
                    enemy.updateDirection('R');
                } else if (enemy.direction == 'D') {
                    enemy.updateDirection('L');
                } else if (enemy.direction == 'L') {
                    enemy.updateDirection('U');
                } else if (enemy.direction == 'R') {
                        enemy.updateDirection('D');
                }
                break;
            }
        }
    }

    /*
    public String enemyTurn(Block a, Block b) {
        if (a.x == b.x && a.y == b.y) {
            return "R";
        }
    }
    */

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

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {

        System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == 87) {
            selector.y -= tileSize;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == 83) {
            selector.y += tileSize;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == 65) {
            selector.x -= tileSize;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == 68) {
            selector.x += tileSize;
        }
        /*
        for (int i = 0; i < 10; i++) {
            if (e.getKeyCode() == 49+i) {
                selector.x = emptyTowers.get(i).x;
                selector.y = emptyTowers.get(i).y;
            } else if (e.getKeyCode() == 48) {
                selector.x = emptyTowers.get(9).x;
                selector.y = emptyTowers.get(9).y;
            }
        }
        */
        if (e.getKeyCode() == 82) {
            if (isTileOnType(selector, "tower")) {
                System.out.println("Theres already a tower there");
            } else if (isTileOnType(selector, "road") || isTileOnType(selector, "left") || isTileOnType(selector, "right")) {
                System.out.println("You can't put a tower on roads");
            }
            Tower tower = new Tower(tower1Image, selector.x, selector.y, tileSize, tileSize, "tower1", 10, 1, 5);
            towers.add(tower);
        }

        if (e.getKeyCode() == 84) {
            if (isTileOnType(selector, "tower")) {
                System.out.println("Theres already a tower there");
            } else if (isTileOnType(selector, "road") || isTileOnType(selector, "left") || isTileOnType(selector, "right")) {
                System.out.println("You can't put a tower on roads");
            }
            Tower tower = new Tower(tower2Image, selector.x, selector.y, tileSize, tileSize, "tower2", 20, 1, 3);
            towers.add(tower);
        }
    }
}