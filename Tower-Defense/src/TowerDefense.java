import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;

public class TowerDefense extends JPanel implements ActionListener, KeyListener {
    class Tile {
        int x;
        int y;
        int width;
        int height;
        Image image;
        char type;

        char direction = 'R'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Tile(Image image, int x, int y, int width, int height, char type) {
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

    class Enemy extends Tile {
        int health;
        int priority = 0;
        double speed;

        Enemy(Image image, int x, int y, int width, int height, char type, int health, double speed) {
            super(image, x, y, width, height, type);
            this.health = health;
            this.speed = speed;
        }
    }

    class Tower extends Tile {
        int damage;
        int range;
        int fireRate;
        boolean doesAOE;

        Tower(Image image, int x, int y, int width, int height, char type, int damage, int fireRate, int range, boolean doesAOE) {
            super(image, x, y, width, height, type);
            this.damage = damage;
            this.fireRate = fireRate;
            this.range = range;
            this.doesAOE = doesAOE;
        }
    }

    private int rowCount = 11;
    private int columnCount = 12;
    private int tileSize = 64;
    private int boardWidth = (columnCount+6) * tileSize - tileSize * 2;
    private int boardHeight = (rowCount+1) * tileSize - tileSize * 2;

    private int animationCounter = 0;
    private int timer = 0;

    private Image roadImage;
    private Image leftImage;
    private Image rightImage;
    private Image groundImage;
    private Image tower1Image;
    private Image tower2Image;
    private Image enemy1Image;
    private Image selectorImage1;
    private Image selectorImage2;

    //# = road, L = left turn, R = right turn, T = tower, ' ' = ground
    private char[][] tileMap = {
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#',' '},
            {' ',' ','R','#','#','#','R',' ',' ',' ','#',' '},
            {' ',' ','#',' ',' ',' ','L','#','#','#','L',' '},
            {' ',' ','#',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ','#',' ',' ',' ','L','#','#','#','L',' '},
            {' ',' ','R','#','#','#','R',' ',' ',' ','#',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ','#',' '},
            {'#','#','#','#','#','#','R',' ',' ',' ','#',' '},
            {' ',' ',' ',' ',' ',' ','L','#','#','#','L',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '}
    };

    private int waveNumber = 1;
    private ArrayList<Integer> wave;
    private int waveCounter = 0;

    ArrayList<Tile> tiles = new ArrayList<Tile>();
    ArrayList<Tower> towers = new ArrayList<Tower>();
    ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    Tile selector;

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
        leftImage = new ImageIcon(getClass().getResource("./assets/left.png")).getImage();
        rightImage = new ImageIcon(getClass().getResource("./assets/right.png")).getImage();
        groundImage = new ImageIcon(getClass().getResource("./assets/ground.png")).getImage();
        tower1Image = new ImageIcon(getClass().getResource("./assets/tower1.png")).getImage();
        tower2Image = new ImageIcon(getClass().getResource("./assets/tower2.png")).getImage();
        enemy1Image = new ImageIcon(getClass().getResource("./assets/enemy1.png")).getImage();
        selectorImage1 = new ImageIcon(getClass().getResource("./assets/selector1.png")).getImage();
        selectorImage2 = new ImageIcon(getClass().getResource("./assets/selector2.png")).getImage();
        loadMap();
        createWave();

        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                if (tileMap[i][j] == '#') {
                    Tile road = new Tile(roadImage, j*tileSize-tileSize/2, i*tileSize-tileSize/2, tileSize, tileSize, '#');
                    tiles.add(road);
                } else if (tileMap[i][j] == 'L') {
                    Tile road = new Tile(leftImage, j*tileSize-tileSize/2, i*tileSize-tileSize/2, tileSize, tileSize, 'L');
                    tiles.add(road);
                } else if (tileMap[i][j] == 'R') {
                    Tile road = new Tile(rightImage, j*tileSize-tileSize/2, i*tileSize-tileSize/2, tileSize, tileSize, 'L');
                    tiles.add(road);
                } else if (tileMap[i][j] == ' ') {
                    Tile ground = new Tile(groundImage, j*tileSize-tileSize/2, i*tileSize-tileSize/2, tileSize, tileSize, ' ');
                    tiles.add(ground);
                }
            }
        }
        /*
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {

                int x = c * tileSize - tileSize / 2;
                int y = r * tileSize - tileSize / 2;

                if(r < rowCount && c < columnCount) {
                    String row = tileMap[r];
                    char tileMapChar = row.charAt(c);
                    if (tileMapChar == '#') { //block road
                        Tile road = new Tile(roadImage, x, y, tileSize, tileSize, "road");
                        tiles.add(road);
                    } else if (tileMapChar == 'L') { //block road
                        Tile road = new Tile(leftImage, x, y, tileSize, tileSize, "left");
                        tiles.add(road);
                    } else if (tileMapChar == 'R') { //block road
                        Tile road = new Tile(rightImage, x, y, tileSize, tileSize, "right");
                        tiles.add(road);
                    } else if (tileMapChar == ' ') { //ground
                        Tile ground = new Tile(null, x, y, 8, 8, "ground");
                        tiles.add(ground);
                    } else if (tileMapChar == '1') { //enemy1
                        Tile ground = new Tile(null, x, y, 8, 8, "ground");
                        tiles.add(ground);
                        Enemy enemy1 = new Enemy(enemy1Image, x, y, tileSize, tileSize, "enemy1", 100, 1);
                        enemies.add(enemy1);
                    }
                } else {
                    Tile ground = new Tile(null, x, y, 8, 8, "ground");
                    tiles.add(ground);
                }
            }
        }
        */
        selector = new Tile(selectorImage1, tileSize/2, tileSize/2, tileSize, tileSize, 'S');
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        for (Tile tile : tiles) {
            g.drawImage(tile.image, tile.x, tile.y, tile.width, tile.height, null);
        }
        for (Tower tower : towers) {
            g.drawImage(tower.image, tower.x, tower.y, tower.width, tower.height, null);
        }
        for (Enemy enemy : enemies) {
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

    public boolean isSelectorIsOnTileOfType(char type) {
        int x = (selector.x - tileSize / 2) / tileSize + 1;
        int y = (selector.y - tileSize / 2) / tileSize + 1;
        // for testing
        /*if (tileMap[y][x] == type) {
            System.out.println("true");
            System.out.println(tileMap[y][x]);
            System.out.println(x + " " + y);
        } else {
            System.out.println("false");
            System.out.println(tileMap[y][x]);
            System.out.println(x + " " + y);
        }*/
        return (tileMap[y][x] == type);
    }

    public boolean isEnemyIsOnTileOfType(char type, Enemy enemy) {
        if(((enemy.x - tileSize / 2) + (enemy.y - tileSize / 2)) % tileSize == 0) {
            int x = (enemy.x - tileSize / 2) / tileSize + 1;
            int y = (enemy.y - tileSize / 2) / tileSize + 1;
            return (tileMap[y][x] == type);
        }
        return false;
    }

    public boolean enemyInRange(Tower tower, Enemy enemy) {
        if (tower.range == 1) {
            if (enemy.x + enemy.width > tower.x &&
                    enemy.x < tower.x + tower.width &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize ||
                    enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y &&
                    enemy.y < tower.y + tower.height) {
                return true;
            }
        } else if (tower.range == 2) {
            if (enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize) {
                return true;
            }
        } else if (tower.range == 3) {
            if (enemy.x + enemy.width > tower.x &&
                    enemy.x < tower.x + tower.width &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize ||
                    enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y &&
                    enemy.y < tower.y + tower.height ||
                    enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize) {
                return true;
            }
        } else if (tower.range == 4) {
            if (enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y - tileSize*2 &&
                    enemy.y < tower.y + tower.height + tileSize*2 ||
                    enemy.x + enemy.width > tower.x - tileSize*2 &&
                    enemy.x < tower.x + tower.width + tileSize*2 &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize) {
                return true;
            }
        } else if (tower.range == 5) {
            if (enemy.x + enemy.width > tower.x - tileSize*2 &&
                    enemy.x < tower.x + tower.width + tileSize*2 &&
                    enemy.y + enemy.height > tower.y - tileSize*2 &&
                    enemy.y < tower.y + tower.height + tileSize*2) {
                return true;
            }
        } else if (tower.range == 6) {
            if (enemy.x + enemy.width > tower.x &&
                    enemy.x < tower.x + tower.width &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize ||
                    enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y &&
                    enemy.y < tower.y + tower.height ||
                    enemy.x + enemy.width > tower.x - tileSize*2 &&
                    enemy.x < tower.x + tower.width + tileSize*2 &&
                    enemy.y + enemy.height > tower.y - tileSize*2 &&
                    enemy.y < tower.y + tower.height + tileSize*2) {
                return true;
            }
        } else if (tower.range == 7) {
            if (enemy.x + enemy.width > tower.x - tileSize &&
                    enemy.x < tower.x + tower.width + tileSize &&
                    enemy.y + enemy.height > tower.y - tileSize*2 &&
                    enemy.y < tower.y + tower.height + tileSize*2 ||
                    enemy.x + enemy.width > tower.x - tileSize*2 &&
                    enemy.x < tower.x + tower.width + tileSize*2 &&
                    enemy.y + enemy.height > tower.y - tileSize &&
                    enemy.y < tower.y + tower.height + tileSize ||
                    enemy.x + enemy.width > tower.x - tileSize*2 &&
                    enemy.x < tower.x + tower.width + tileSize*2 &&
                    enemy.y + enemy.height > tower.y - tileSize*2 &&
                    enemy.y < tower.y + tower.height + tileSize*2) {
                return true;
            }
        } else if (tower.range == 8) {
            if (enemy.x + enemy.width > tower.x - tileSize*2 &&
                    enemy.x < tower.x + tower.width + tileSize*2 &&
                    enemy.y + enemy.height > tower.y - tileSize*3 &&
                    enemy.y < tower.y + tower.height + tileSize*3 ||
                    enemy.x + enemy.width > tower.x - tileSize*3 &&
                    enemy.x < tower.x + tower.width + tileSize*3 &&
                    enemy.y + enemy.height > tower.y - tileSize*2 &&
                    enemy.y < tower.y + tower.height + tileSize*2) {
                return true;
            }
        } else if (tower.range == 9) {
            if (enemy.x + enemy.width > tower.x - tileSize*3 &&
                    enemy.x < tower.x + tower.width + tileSize*3 &&
                    enemy.y + enemy.height > tower.y - tileSize*3 &&
                    enemy.y < tower.y + tower.height + tileSize*3) {
                return true;
            }
        }
        return false;
    }

    public void move() {
        for(Enemy enemy : enemies) {
            if(enemy.velocityX == 0 && enemy.velocityY == 0) {
                enemy.velocityX = tileSize/16;
            }
            enemy.x += enemy.velocityX;
            enemy.y += enemy.velocityY;

            if (isEnemyIsOnTileOfType('L', enemy)) {
                if (enemy.direction == 'U') {
                    enemy.updateDirection('L');
                } else if (enemy.direction == 'D') {
                    enemy.updateDirection('R');
                } else if (enemy.direction == 'L') {
                    enemy.updateDirection('D');
                } else if (enemy.direction == 'R') {
                    enemy.updateDirection('U');
                }
            } else if (isEnemyIsOnTileOfType('R', enemy)) {
                if (enemy.direction == 'U') {
                    enemy.updateDirection('R');
                } else if (enemy.direction == 'D') {
                    enemy.updateDirection('L');
                } else if (enemy.direction == 'L') {
                    enemy.updateDirection('U');
                } else if (enemy.direction == 'R') {
                    enemy.updateDirection('D');
                }
            }
        }
    }

    public void attack() {
        for (Tower tower : towers) {
            for (int i = 0; i < enemies.size(); i++) {
                if (enemyInRange(tower, enemies.get(i))) {
                    enemies.get(i).health-=tower.damage;
                    System.out.println(enemies.get(i).health);
                    if(enemies.get(i).health <= 0) {
                        enemies.remove(i);
                    }
                }
            }
        }
    }

    public void createWave() {
        wave = new ArrayList<Integer>();
        for (int i = 0; i < waveNumber*50 + 1000; i++) {
            wave.add(1);
            for (int j = 0; j < (int) (Math.random()*51) && i < waveNumber*50 + 1000; j++) {
                wave.add(0);
                i++;
            }
        }
    }

    public void summon() {
        if(wave.get(waveCounter) == 1) {
            Enemy enemy = new Enemy(enemy1Image, tileSize / 2, tileSize * 7 + tileSize / 2, tileSize, tileSize, '1', 200, 1);
            enemies.add(enemy);
            System.out.println("1");
        } else if(wave.get(waveCounter) == 2) {
            Enemy enemy = new Enemy(enemy1Image, tileSize / 2, tileSize * 7 + tileSize / 2, tileSize, tileSize, '2', 150, 1.5);
            enemies.add(enemy);
        }
        waveCounter++;
        System.out.println(waveCounter);
        System.out.println(wave.get(waveCounter));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        attack();
        summon();
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

        int x = (selector.x - tileSize / 2) / tileSize + 1;
        int y = (selector.y - tileSize / 2) / tileSize + 1;

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

        if (e.getKeyCode() == 82) {
            if (isSelectorIsOnTileOfType('1') || isSelectorIsOnTileOfType('2')) {
                System.out.println("Theres already a tower there");
            } else if (isSelectorIsOnTileOfType('#') || isSelectorIsOnTileOfType('L') || isSelectorIsOnTileOfType('R')) {
                System.out.println("You can't put a tower on roads");
            } else {
                Tower tower = new Tower(tower1Image, selector.x, selector.y, tileSize, tileSize, '1', 10, 1, 5, false);
                tileMap[y][x] = '1';
                towers.add(tower);
            }
        }

        if (e.getKeyCode() == 84) {
            if (isSelectorIsOnTileOfType('1') || isSelectorIsOnTileOfType('2')) {
                System.out.println("Theres already a tower there");
            } else if (isSelectorIsOnTileOfType('#') || isSelectorIsOnTileOfType('L') || isSelectorIsOnTileOfType('R')) {
                System.out.println("You can't put a tower on roads");
            } else {
                Tower tower = new Tower(tower2Image, selector.x, selector.y, tileSize, tileSize, '2', 20, 1, 3, true);
                tileMap[y][x] = '2';
                towers.add(tower);
            }
        }
    }
}