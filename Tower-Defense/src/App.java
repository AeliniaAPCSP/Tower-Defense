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
//To do

//Making the game balanced better
//Add way more enemies and towers
//Making the game’s art, theme, and vibe more unique and better
//Actual projectiles
//Damage indicators of some form
//More animated sprites
//Sounds and music
//Making the paths have curves where turns are
//Map generation
//More gambling
//Soooo many more features to come