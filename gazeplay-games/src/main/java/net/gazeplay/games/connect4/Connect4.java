package net.gazeplay.games.connect4;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import net.gazeplay.GameLifeCycle;
import net.gazeplay.IGameContext;
import net.gazeplay.commons.ui.I18NLabel;
import net.gazeplay.commons.utils.stats.Stats;

import java.util.ArrayList;

public class Connect4 implements GameLifeCycle {

    private final Stats stats;
    private final IGameContext gameContext;

    // Parameters
    private final int nbRows = 6;
    private final int nbColumns = 7;
    private final Color player1Color = Color.RED;
    private final Color player2Color = Color.ORANGE;
    private final Color grid1Color = Color.BLUE;
    private double fallingDuration = 300.0;

    // Grid management
    private int[][] grid;
    private double gridWidth;
    private double gridHeight;
    private double gridXOffset;
    private double gridYOffset;
    private double cellSize;

    // Display
    private Timeline progressTimeline;
    private ProgressIndicator progressIndicator;
    private ArrayList<Rectangle> columnPicker;
    private Rectangle gridRectangle;
    private Pane gridPane;
    private Pane topPane;

    // Game management
    private IntegerProperty currentPlayer;

    Connect4(final IGameContext gameContext, final Stats stats){
        this.stats = stats;
        this.gameContext = gameContext;
        grid = new int[nbColumns][nbRows];
        columnPicker = new ArrayList<>();
        currentPlayer = new SimpleIntegerProperty(1);
    }

    Connect4(final IGameContext gameContext, final Stats stats, double gameSeed){
        this.stats = stats;
        this.gameContext = gameContext;
    }

    @Override
    public void launch() {

        updateSize();

        BorderPane mainPane = new BorderPane();
        gameContext.getChildren().add(mainPane);

        gridPane = new Pane();
        mainPane.setCenter(gridPane);

        // Right pane
        VBox rightPane = new VBox();
        mainPane.setRight(rightPane);
        rightPane.setAlignment(Pos.CENTER);

        Label currentPlayerLabel = new I18NLabel(gameContext.getTranslator(), "CurrentPlayer");
        currentPlayerLabel.setTextFill(Color.WHITE);
        currentPlayerLabel.setFont(new Font(30));
        currentPlayerLabel.setPadding(new Insets(0,50,0,50));
        rightPane.getChildren().add(currentPlayerLabel);

        Circle currentPlayerCircle = new Circle();
        currentPlayerCircle.setRadius(cellSize*0.6);
        currentPlayerCircle.setFill(player1Color);
        rightPane.getChildren().add(currentPlayerCircle);
        currentPlayer.addListener(observable -> {
            currentPlayerCircle.setFill(currentPlayer.getValue() == 1 ? player1Color:player2Color);
        });

//        // For dev purposes
//        Slider slider = new Slider(0,2000,10);
//        slider.valueProperty().addListener(new ChangeListener<Number>() {
//            @Override
//            public void changed(ObservableValue<? extends Number> observableValue, Number oldVal, Number newVal) {
//                fallingDuration = (double) newVal;
//                System.out.println("falling duration : "+fallingDuration);
//            }
//        });
//        mainPane.setLeft(slider);

        topPane = new Pane();
        mainPane.setTop(topPane);

        // Create grid rectangle
        gridRectangle = new Rectangle(gridXOffset, 0, gridWidth, gridHeight);
        gridRectangle.setFill(grid1Color);
        gridPane.getChildren().add(gridRectangle);

        // Create progress indicator
        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setStyle(" -fx-progress-color: " + gameContext.getConfiguration().getProgressBarColor());
        progressIndicator.setOpacity(0);
        progressIndicator.setMinSize(cellSize,cellSize);

        // Create columnPicker
        for(int i=0; i<nbColumns; i++){
            Group group = new Group();
            Rectangle topRectangle = new Rectangle(gridXOffset + i*cellSize,0,cellSize,gridYOffset);
            topRectangle.setFill(Color.color(Math.random(), Math.random(), Math.random()));
            int tempi = i;
            group.addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    //Progress Indicator
                    double progressSize = Math.min(topRectangle.getHeight(), topRectangle.getWidth());
                    progressIndicator.setTranslateX(gridXOffset+topRectangle.getWidth()*tempi+(topRectangle.getWidth()-progressSize)/2);
                    progressIndicator.setTranslateY((topRectangle.getHeight()-progressSize)/2);
                    progressIndicator.setMinSize(progressSize,progressSize);
                    progressIndicator.setProgress(0);
                    progressIndicator.setOpacity(1);
                    group.getChildren().add(progressIndicator);

                    progressTimeline = new Timeline();
                    progressTimeline.getKeyFrames().add(new KeyFrame(new Duration(gameContext.getConfiguration().getFixationLength()), new KeyValue(progressIndicator.progressProperty(), 1)));
                    progressTimeline.setOnFinished(actionEvent -> {
                        progressIndicator.setOpacity(0);
                        putToken(tempi);
                    });

                    progressTimeline.play();
                }
            });

            group.addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        progressTimeline.stop();
                        progressIndicator.setOpacity(0);
                        group.getChildren().remove(progressIndicator);
                    }
            });

            columnPicker.add(topRectangle);
            group.getChildren().add(topRectangle);
            topPane.getChildren().add(group);
        }

        // Resize events
//        mainPane.heightProperty().addListener((obs, oldVal, newVal) -> {
//            updateSize();
//            updateGrid();
//            updateColumnPicker();
//        });

        updateGrid();
    }

    @Override
    public void dispose() {
        gameContext.getChildren().clear();
    }

    public void updateSize(){
        Dimension2D dimension2D = gameContext.getGamePanelDimensionProvider().getDimension2D();
        cellSize = Math.min(dimension2D.getWidth()*0.8/nbColumns, dimension2D.getHeight()*0.85/nbRows);
        gridWidth = cellSize*nbColumns;
        gridHeight = cellSize*nbRows;
        gridXOffset = (dimension2D.getWidth() - gridWidth)/2;
        gridYOffset = dimension2D.getHeight()-gridHeight;
    }

    public void updateColumnPicker(){
        for (int i = 0; i < columnPicker.size(); i++) {
            columnPicker.get(i).setTranslateX(gridXOffset + i*cellSize);
            columnPicker.get(i).setTranslateY(0);
            columnPicker.get(i).setWidth(cellSize);
            columnPicker.get(i).setHeight(gridYOffset);
        }
    }

    public void clearGrid(){
        for(int i = 0; i<nbColumns; i++){
            for(int j = 0; j<nbRows; j++){
                grid[i][j] = 0;
            }
        }
    }

    public void updateGrid(){
        // Resize grid
//        gridRectangle.setTranslateX(gridXOffset);
//        gridRectangle.setTranslateY(0);
//        gridRectangle.setWidth(gridWidth);
//        gridRectangle.setHeight(gridHeight);

        // Create tokens
        Color color;
        for(int i = 0; i<nbColumns; i++){
            for(int j = 0; j<nbRows; j++){
                switch(grid[i][j]){
                    case 0:
                        color = Color.WHITE;
                        break;
                    case 1:
                        color = player1Color;
                        break;
                    case 2:
                        color = player2Color;
                        break;
                    default:
                        color = Color.WHITE;
                }
                double radius = cellSize*0.4;
                double centerx = gridXOffset + (i+0.5)*cellSize;
                double centery = (j+0.5)*cellSize;
                Circle c = new Circle(centerx,centery,radius);
                c.setFill(color);
                gridPane.getChildren().add(c);
            }
        }
    }

    public void putToken(int column){
        int j = nbRows-1;
        while(j>=0){
            if(grid[column][j]==0){

                // Update the grid
                grid[column][j]=currentPlayer.getValue();

                // Play animation
                double radius = cellSize*0.4;
                double centerx = gridXOffset + (column+0.5)*cellSize;
                double centery = 0.5*cellSize;
                Circle c = new Circle(centerx,centery,radius);
                c.setFill(currentPlayer.getValue() == 1 ? player1Color:player2Color);
                gridPane.getChildren().add(c);

                TranslateTransition transition = new TranslateTransition();
                transition.setDuration(Duration.millis(fallingDuration/nbRows*(j+1)));
                transition.setNode(c);

                transition.setByY((j+1)*cellSize);
                transition.setOnFinished(e -> {
                    updateGrid();
                    gameContext.getSoundManager().add("data/connect4/sounds/tokenFalling.wav");
                });
                transition.play();
                break;
            }
            j--;
        }

        if(checkVictory()!=0){
            gameContext.playWinTransition(50, e -> restart());
            System.out.println("Player "+(checkVictory()==1 ? "RED":"ORANGE")+" won");
            // TODO currentPlayer won
        }
        currentPlayer.set(1 + currentPlayer.getValue()%2);
    }

    public int checkVictory(){
        int[][] directions = {{1,0}, {1,-1}, {1,1}, {0,1}};
        for (int[] d : directions) {
            int dx = d[0];
            int dy = d[1];
            for (int x = 0; x < nbColumns; x++) {
                for (int y = 0; y < nbRows; y++) {
                    int lastx = x + 3*dx;
                    int lasty = y + 3*dy;
                    if (0 <= lastx && lastx < nbColumns && 0 <= lasty && lasty < nbRows) {
                        int w = grid[x][y];
                        if (w != 0 && w == grid[x+dx][y+dy]
                            && w == grid[x+2*dx][y+2*dy]
                            && w == grid[lastx][lasty]) {
                            return w;
                        }
                    }
                }
            }
        }
        return 0;
    }

    private void restart(){
        gameContext.endWinTransition();
        clearGrid();
        launch();
        updateGrid();
        currentPlayer.setValue(1);
    }

}
