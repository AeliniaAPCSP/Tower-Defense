import javax.swing.JFrame;

public class App {
    public static void main(String[] args) throws Exception {

        JFrame frame = new JFrame("Tower Defense");
        //frame.setVisible(true);
        //frame.setSize(boardWidth, boardHeight);
        frame.setLocation(448, 188);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TowerDefense towerDefenseGame = new TowerDefense();
        frame.add(towerDefenseGame);
        frame.pack();
        towerDefenseGame.requestFocus();
        frame.setVisible(true);

    }
}