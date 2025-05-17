import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {
        //int rowCount = 12;
        //int columnCount = 17;
        //int tileSize = 64;
        //int boardWidth = columnCount * tileSize - tileSize;
        //int boardHeight = rowCount * tileSize - tileSize;

        JFrame frame = new JFrame("Tower Defense");
        // frame.setVisible(true);
        //frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TowerDefense towerDefenseGame = new TowerDefense();
        frame.add(towerDefenseGame);
        frame.pack();
        towerDefenseGame.requestFocus();
        frame.setVisible(true);

    }
}