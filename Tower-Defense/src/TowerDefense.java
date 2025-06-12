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
            int temp = Math.abs(this.velocityX + this.velocityY);
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -temp;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = temp;
            }
            else if (this.direction == 'L') {
                this.velocityX = -temp;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = temp;
                this.velocityY = 0;
            }
        }
    }

    class Enemy extends Tile {
        int health;
        int priority;
        int speed;
        int step = 0;

        Enemy(Image image, int x, int y, int width, int height, char type, int health, int speed) {
            super(image, x, y, width, height, type);
            this.health = health;
            this.speed = speed;
            priority = 0;
        }
    }

    class Tower extends Tile {
        int damage;
        int range;
        double fireRate;
        boolean doesAOE;

        Tower(Image image, int x, int y, int width, int height, char type, int damage, double fireRate, int range, boolean doesAOE) {
            super(image, x, y, width, height, type);
            this.damage = damage;
            this.fireRate = fireRate;
            this.range = range;
            this.doesAOE = doesAOE;
        }
    }

    //variables for the board
    private int rowCount = 12;
    private int columnCount = 14;
    private int tileSize = 64;
    private int boardWidth = (columnCount+2) * tileSize;
    private int boardHeight = (rowCount-1) * tileSize;

    private int animationCounter = 0;
    private int gold = 30;
    private int confirmation = 0;
    private int baseHealth = 10;
    private String output = "";
    private int outputTimer = 0;

    //Image variable creation
    private Image roadImage = new ImageIcon(getClass().getResource("./assets/road.png")).getImage();
    //private Image leftImage = new ImageIcon(getClass().getResource("./assets/left.png")).getImage();
    //private Image rightImage = new ImageIcon(getClass().getResource("./assets/right.png")).getImage();
    private Image groundImage = new ImageIcon(getClass().getResource("./assets/ground.png")).getImage();
    private Image tower1Image = new ImageIcon(getClass().getResource("./assets/tower1.png")).getImage();
    private Image tower2Image = new ImageIcon(getClass().getResource("./assets/tower2.png")).getImage();
    private Image enemy1Image = new ImageIcon(getClass().getResource("./assets/enemy1.png")).getImage();
    private Image enemy2Image = new ImageIcon(getClass().getResource("./assets/enemy2.png")).getImage();
    private Image selectorImage1 = new ImageIcon(getClass().getResource("./assets/selector1.png")).getImage();
    private Image selectorImage2 = new ImageIcon(getClass().getResource("./assets/selector2.png")).getImage();
    private Image enemySpawnerImage = new ImageIcon(getClass().getResource("./assets/enemySpawner.png")).getImage();

    //# = road, L = left turn, R = right turn, ' ' = ground
    //1 = tower1, 2 = tower2 but those are only added to tileMap during the game
    private char[][] tileMap = {
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ','R','#','#','#','R',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ','#',' ',' ',' ','#',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ','#',' ',' ',' ','L','#','#','#'},
            {' ',' ',' ','R','#','R',' ','#',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ','#',' ','#',' ','#',' ',' ',' ',' ',' ',' ',' '},
            {'$','#','#','L',' ','#',' ','#',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ','#',' ','#',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ','L','#','L',' ',' ',' ',' ',' ',' ',' '},
            {' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' ',' '}
    };

    private int mapMove = 0;
    private int mapMovementCounter = 0;
    private int mapShifts = 0;

    //for the enemy waves
    private int waveNumber = 1;
    private ArrayList<Integer> wave;
    private int waveCounter = -20;

    ArrayList<Tile> tiles = new ArrayList<Tile>();
    ArrayList<Tower> towers = new ArrayList<Tower>();
    ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    Tile enemySpawner;
    Tile selector;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right

    TowerDefense() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        //bright pink because this is only visible when something goes wrong
        setBackground(new Color(255, 0, 255));
        addKeyListener(this);
        setFocusable(true);

        loadMap();
        createWave();

        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(40, this); //25fps (1000/40)
        gameLoop.start();
    }

    public void loadMap() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                if (tileMap[i][j] == '#') {
                    Tile road = new Tile(roadImage, j*tileSize, i*tileSize - tileSize/2, tileSize, tileSize, '#');
                    tiles.add(road);
                } else if (tileMap[i][j] == 'L') {
                    Tile road = new Tile(roadImage, j*tileSize, i*tileSize - tileSize/2, tileSize, tileSize, 'L');
                    tiles.add(road);
                } else if (tileMap[i][j] == 'R') {
                    Tile road = new Tile(roadImage, j*tileSize, i*tileSize - tileSize/2, tileSize, tileSize, 'L');
                    tiles.add(road);
                } else if (tileMap[i][j] == ' ') {
                    Tile ground = new Tile(groundImage, j*tileSize, i*tileSize - tileSize/2, tileSize, tileSize, ' ');
                    tiles.add(ground);
                } else if (tileMap[i][j] == '$') {
                    enemySpawner = new Tile(enemySpawnerImage,  j*tileSize, i*tileSize - tileSize/2, tileSize, tileSize, '$');
                }
            }
        }

        if(mapShifts == 0){
            selector = new Tile(selectorImage1, tileSize, tileSize/2, tileSize, tileSize, 'S');
        } else if(selector.x == 0){
            selector.x = tileSize;
        }
    }

    public void mapShift() {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount-1; j++) {
                tileMap[i][j] = tileMap[i][j+1];
            }
        }

        int numberOfRoads = 0;
        for (int i = 0; i < rowCount; i++) {
            if (tileMap[i][0] == '#' || tileMap[i][0] == 'L' || tileMap[i][0] == 'R') {
                numberOfRoads++;
            }
        }
        if (numberOfRoads == 1) {
            for (int i = 0; i < rowCount; i++) {
                if (tileMap[i][0] == '#') {
                    tileMap[i][0] = '$';
                    enemySpawner.x = tileSize * -2;
                    enemySpawner.y = i*tileSize - tileSize/2;
                }
            }
        }

        for (int i = 0; i < rowCount; i++) {
            tileMap[i][columnCount-1] = ' ';
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        for (Tile tile : tiles) {
            g.drawImage(tile.image, tile.x - tileSize, tile.y, tile.width, tile.height, null);
        }
        for (Tower tower : towers) {
            g.drawImage(tower.image, tower.x - tileSize, tower.y, tower.width, tower.height, null);
        }
        for (Enemy enemy : enemies) {
            g.drawImage(enemy.image, enemy.x, enemy.y, enemy.width, enemy.height, null);
        }
        g.drawImage(enemySpawner.image, enemySpawner.x - enemySpawner.width, enemySpawner.y, enemySpawner.width, enemySpawner.height, null);

        //places the info panel on the right of the board
        g.drawImage(new ImageIcon(getClass().getResource("./assets/towerPanel.png")).getImage(), tileSize * (columnCount - 2), 0, tileSize * 4, tileSize * rowCount-tileSize, null);

        //animates the selector
        if (animationCounter > 7) {
            g.drawImage(selectorImage1, selector.x, selector.y, selector.width, selector.height, null);
        } else {
            g.drawImage(selectorImage2, selector.x, selector.y, selector.width, selector.height, null);
        }
        animationCounter++;
        if (animationCounter == 16) {
            animationCounter = 0;
        }

        //displays numbers in top left
        g.setFont(new Font("Arial", Font.BOLD, 16 * tileSize / 64));
        g.drawString("Gold: " + gold + "   Current Wave: " + waveNumber + "   Health Left: " + baseHealth, tileSize/8, tileSize/8 + tileSize/4);

        //displays messages to player on bottom left
        if(!(output.equals("")) && outputTimer > 0) {
            g.setFont(new Font("Arial", Font.BOLD, 32 * tileSize / 64));
            g.drawString(output, tileSize/8, tileSize * rowCount - tileSize - tileSize/4);
            outputTimer--;
        } else if (!(output.equals(""))) {
            output = "";
        }

        if(baseHealth <= 0){
            g.drawImage(new ImageIcon(getClass().getResource("./assets/deathScreen.png")).getImage(), 0, 0, tileSize * (columnCount + 2), tileSize * rowCount, null);
        }
    }

    //pretty self-explanatory
    public void killEnemyIfHealth0() {
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i).health <= -10000) {
                enemies.remove(i);
            } else if (enemies.get(i).health <= 0) {
                if (enemies.get(i).type == '1') {
                    gold++;
                } else if (enemies.get(i).type == '2') {
                    gold++;
                }
                enemies.remove(i);
            }
        }
    }

    //Checks if the selector is on the type of tile indicated by char type
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

    //Checks if the enemy entered is on the type of tile indicated by char type
    public boolean isEnemyIsOnTileOfType(char type, Enemy enemy) {
        int x = (enemy.x - tileSize/2 + mapMovementCounter) / tileSize + 2;
        int y = (enemy.y - tileSize/2) / tileSize + 1;
        System.out.println("mapMovementCounter: " + mapMovementCounter);
        System.out.println("x: " + x);
        System.out.println("y: " + y);
        if(x >= 0 && y >= 0) {
            return (tileMap[y][x] == type);
        }
        return false;
    }

    //moves all the enemies
    public void move() {
        for(Enemy enemy : enemies) {
            if(enemy.x >= columnCount * tileSize - tileSize - mapMovementCounter
                    /*
                    || enemy.y <= -enemy.height ||
                    enemy.y >= rowCount * tileSize - tileSize
                    */
                    ) {
                enemy.health = -10000;
                baseHealth--;
            } else {
                if (enemy.velocityX == 0 && enemy.velocityY == 0) {
                    if(enemy.type == '1') {
                        enemy.velocityX = tileSize / 16;
                    } else if (enemy.type == '2') {
                        enemy.velocityX = tileSize / 8;
                    }
                }

                enemy.x += enemy.velocityX;
                enemy.y += enemy.velocityY;

                if (((enemy.x + mapMovementCounter) + (enemy.y - tileSize/2)) % tileSize == 0) {
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
        }
        //updates all enemy priority values
        for(Enemy enemy : enemies) {
            if(enemy.type == '1') {
                enemy.priority++;
            } else if (enemy.type == '2') {
                enemy.priority+=2;
            }
        }
    }

    //makes all the towers attack any enemies within their ranges
    public void attack() {
        /*
        if (enemies.size() != 0) {
            int highestPriority = 0;
            int enemyIndex = -1;
            for (Tower tower : towers) {
                if (tower.doesAOE == true) {
                    for (int i = 0; i < enemies.size(); i++) {
                        if (enemyInRange(tower, enemies.get(i))) {
                            enemies.get(i).health -= tower.damage;
                        }
                    }
                } else if (tower.doesAOE == false) {
                    for (int i = 0; i < enemies.size(); i++) {
                        //System.out.println("check 1: " + enemyInRange(tower, enemies.get(i)));
                        if (enemyInRange(tower, enemies.get(i))) {
                            if (enemies.get(i).priority > highestPriority) {
                                highestPriority = enemies.get(i).priority;
                                enemyIndex = i;
                                //System.out.println(enemyIndex);
                            }
                        }
                    }
                    if (enemyIndex != -1) {
                        if (enemies.size() != 0) {
                            enemies.get(enemyIndex).health -= tower.damage;
                            //System.out.println(enemies.get(i).health);
                        }
                    }
                }
            }
            killEnemyIfHealth0();
        }
        */
    }

    //creates a long ArrayList that that summon() will read through to
    //decide when to summon an enemy or not and which type to summon
    public void createWave() {
        wave = new ArrayList<Integer>();
        for (int i = 0; i < waveNumber*50 + 100; i++) {
            //for when I add more enemies
            /*if(waveNumber >= 10) {
                if(Math.random() > 0.33) {
                    wave.add(3);
                } else  if (Math.random() > 0.5) {
                    wave.add(2);
                } else {
                    wave.add(1);
                }
            } else*/ if (waveNumber >= 3) {
                if(Math.random() > 0.6) {
                    wave.add(2);
                } else {
                    wave.add(1);
                }
            } else {
                wave.add(1);
            }
            for (int j = 0; j < (int) (Math.random() * (200 * Math.pow(.6, waveNumber))) && i < waveNumber*50 + 100; j++) {
                wave.add(0);
                i++;
            }
        }
    }

    //reads the ArrayList wave that createWave() creates
    public void summon() {
        if (waveCounter < waveNumber*50 + 100 && waveCounter > 0) {
            if (wave.get(waveCounter) == 1) {
                Enemy enemy = new Enemy(enemy1Image, enemySpawner.x - enemySpawner.width, enemySpawner.y, tileSize, tileSize, '1', 375, 0);
                enemies.add(enemy);
            } else if (wave.get(waveCounter) == 2) {
                Enemy enemy = new Enemy(enemy2Image, enemySpawner.x - enemySpawner.width, enemySpawner.y, tileSize, tileSize, '2', 275, 2);
                enemies.add(enemy);
            }
        } else if (waveCounter == waveNumber*50 + 300) {
            waveNumber++;
            waveCounter = 0;
            createWave();
        }
        waveCounter++;
        //System.out.println(waveCounter);
    }

    public void moveMap() {
        for (Enemy enemy : enemies) {
            enemy.x--;
        }
        for (Tower tower : towers) {
            tower.x--;
        }
        for (Tile tile : tiles) {
            tile.x--;
        }
        enemySpawner.x--;
        selector.x--;
        mapMovementCounter++;
    }

    //preforms all the game's constantly repeated functions
    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        attack();
        summon();
        if (mapMove == 3) {
            moveMap();
            mapMove = 0;
        } else {
            mapMove++;
        }
        if(mapMovementCounter >= tileSize) {
            mapMovementCounter = 0;
            mapShifts++;
            mapShift();
            loadMap();
        }
        repaint();
        if (baseHealth <= 0) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {

        int x = (selector.x - tileSize / 2) / tileSize + 1;
        int y = (selector.y - tileSize / 2) / tileSize + 1;

        //System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == 87) {
            if(selector.y != tileSize/2) {
                selector.y -= tileSize;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == 83) {
            if(selector.y != rowCount * tileSize - tileSize*5/2) {
                selector.y += tileSize;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == 65) {
            if(selector.x != tileSize - mapMovementCounter + ((int) mapMovementCounter / tileSize) * tileSize) {
                selector.x -= tileSize;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == 68) {
            if (selector.x != columnCount * tileSize - tileSize*3 - mapMovementCounter) {
                selector.x += tileSize;
            }
        }

        //places tower1
        if (e.getKeyCode() == 82) {
            if (isSelectorIsOnTileOfType('1') || isSelectorIsOnTileOfType('2')) {
                output = "Theres already a tower there";
                outputTimer = 30;
            } else if (isSelectorIsOnTileOfType('#') || isSelectorIsOnTileOfType('L') || isSelectorIsOnTileOfType('R')) {
                output = "You can't put a tower on roads";
                outputTimer = 30;
            } else if (gold < 20) {
                output = "You need 20 gold";
                outputTimer = 30;
            } else {
                Tower tower = new Tower(tower1Image, selector.x, selector.y, tileSize, tileSize, '1', 10, 1, 5, false);
                tileMap[y][x] = '1';
                towers.add(tower);
                gold-=20;
            }
        }

        //places tower2
        if (e.getKeyCode() == 84) {
            if (isSelectorIsOnTileOfType('1') || isSelectorIsOnTileOfType('2')) {
                output = "Theres already a tower there";
                outputTimer = 30;
            } else if (isSelectorIsOnTileOfType('#') || isSelectorIsOnTileOfType('L') || isSelectorIsOnTileOfType('R')) {
                output = "You can't put a tower on roads";
                outputTimer = 30;
            } else if (gold < 50) {
                output = "You need 50 gold";
                outputTimer = 30;
            } else {
                Tower tower = new Tower(tower2Image, selector.x, selector.y, tileSize, tileSize, '2', 2, 1, 2, true);
                tileMap[y][x] = '2';
                towers.add(tower);
                gold-=50;
            }
        }

        //deletes towers when backspace is pressed
        if (e.getKeyCode() == 8) {
            if (isSelectorIsOnTileOfType('1') || isSelectorIsOnTileOfType('2')) {
                if (confirmation == 1) {
                    for (int i = 0; i < towers.size(); i++) {
                        if (towers.get(i).x == selector.x && towers.get(i).y == selector.y) {
                            tileMap[y][x] = ' ';
                            towers.remove(i);
                            if (output.equals("Delete tower?")) {
                                output = "";
                                outputTimer = 0;
                            }
                        }
                    }
                } else {
                    output = "Delete tower?";
                    outputTimer = 30;
                    confirmation = 1;
                }
            }
        }
    }
}