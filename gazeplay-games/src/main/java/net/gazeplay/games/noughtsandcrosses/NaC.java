package net.gazeplay.games.noughtsandcrosses;

import javafx.geometry.Dimension2D;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.random.ReplayablePseudoRandom;
import net.gazeplay.commons.utils.stats.Stats;
import net.gazeplay.components.ProgressButton;


@Slf4j
public class NaC extends Parent implements GameLifeCycle {

    @Getter
    private final IGameContext gameContext;

    private final Stats stats;

    @Getter
    @Setter
    private NaCGameVariant variant;

    private final Dimension2D dimension2D;

    private final ReplayablePseudoRandom random;

    int[][] game;

    boolean player1;

    NaC(final IGameContext gameContext, final Stats stats, final NaCGameVariant variant) {
        this.gameContext = gameContext;
        this.stats = stats;
        this.variant = variant;

        dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();

        random = new ReplayablePseudoRandom();

        int[][] game = new int[][]
            {
                {0,0,0},
                {0,0,0},
                {0,0,0}
            };
    }

    @Override
    public void launch(){
        gameContext.getChildren().clear();

        background();
        button();
        player1 = true;

        stats.notifyNewRoundReady();
        gameContext.getGazeDeviceManager().addStats(stats);
        gameContext.firstStart();
    }

    @Override
    public void dispose(){
        gameContext.getChildren().clear();
    }

    private void win() {
        stats.stop();

        gameContext.updateScore(stats, this);

        gameContext.playWinTransition(500, actionEvent -> {

            dispose();

            gameContext.getGazeDeviceManager().clear();

            gameContext.clear();

            gameContext.showRoundStats(stats, this);
        });
    }

    private void background(){
        Rectangle back = new Rectangle(0,0, dimension2D.getWidth(), dimension2D.getHeight());
        //back.setFill();
        gameContext.getChildren().add(back);
    }

    private void button(){
        //mettre les images
        Image nought;
        Image crosse;
        ProgressButton button00;
        ProgressButton button01;
        ProgressButton button02;
        ProgressButton button10;
        ProgressButton button11;
        ProgressButton button12;
        ProgressButton button20;
        ProgressButton button21;
        ProgressButton button22;
    }

    private void testgame(){
        if (game[0][0] * game[0][1] * game[0][2] == 1 ||
            game[1][0] * game[1][1] * game[1][2] == 1 ||
            game[2][0] * game[2][1] * game[2][2] == 1 ||
            game[0][0] * game[1][0] * game[2][0] == 1 ||
            game[0][1] * game[1][1] * game[2][2] == 1 ||
            game[0][2] * game[1][2] * game[2][1] == 1 ||
            game[0][0] * game[1][1] * game[2][2] == 1 ||
            game[0][2] * game[1][1] * game[2][0] == 1){
                win();
        }
        else if (game[0][0] * game[0][1] * game[0][2] == 8 ||
            game[1][0] * game[1][1] * game[1][2] == 8 ||
            game[2][0] * game[2][1] * game[2][2] == 8 ||
            game[0][0] * game[1][0] * game[2][0] == 8 ||
            game[0][1] * game[1][1] * game[2][2] == 8 ||
            game[0][2] * game[1][2] * game[2][1] == 8 ||
            game[0][0] * game[1][1] * game[2][2] == 8 ||
            game[0][2] * game[1][1] * game[2][0] == 8){
                if (variant.equals(NaCGameVariant.P2)){
                    win();
                } else {
                    restart();
                }
        }
        else if (game[0][0] * game[0][1] * game[0][2] *
            game[1][0] * game[1][1] * game[1][2] *
            game[2][0] * game[2][1] * game[2][2] != 0){
                restart();
        }
    }

    private void robot(){
        int x = 0;
        int y = 0;
        while (game[x][y]!=0){
            x = random.nextInt(3);
            y = random.nextInt(3);
        }
        game[x][y]=2;
        /*
        edit button & permettre joueur de jouer
         */
    }

    private void restart(){
        for (int i=0; i<3; i++){
            for (int j=0; j<3; j++){
                game[i][j]=0;
            }
        }
        /*
        remettre les boutons et enlever les images
         */
    }
}
