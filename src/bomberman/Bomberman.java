/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author fanda
 */
public class Bomberman extends Application {

    public static final int DELAY = 100;
    public static final int UNIT_SIZE = 12;
    private GamePane gamePane;
    private MenuPane menuPane;
    private CreateGamePane createGamePane;
    private ConnectGamePane connectGamePane;
    private final MyGameState game = new MyGameState();
    private Client client;
    public static Scene menuScene, gameScene, createGameScene, connectGameScene, gameOverScene;
    private Stage primaryStage;
    private Text gameoverText;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        gameoverText = new Text("");
        gamePane = new GamePane(UNIT_SIZE, game);
        client = new Client(primaryStage, game, gamePane, gameoverText);
        menuPane = new MenuPane(primaryStage);
        createGamePane = new CreateGamePane(primaryStage, client);
        connectGamePane = new ConnectGamePane(primaryStage, client);

        gameScene = new Scene(gamePane, MyGameState.WIDTH * UNIT_SIZE, MyGameState.HEIGHT * UNIT_SIZE);
        gameScene.setOnKeyPressed(this::dispatchKeyEvents);

        menuScene = new Scene(menuPane, 600, 800);
        createGameScene = new Scene(createGamePane, 600, 800);
        connectGameScene = new Scene(connectGamePane, 600, 800);

        BorderPane gameOverPane = new BorderPane();

        gameOverPane.setCenter(gameoverText);

        gameOverScene = new Scene(gameOverPane, 600, 800);

        gameOverScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                leaveGame();
                primaryStage.setScene(Bomberman.menuScene);
            }
        });

        primaryStage.setTitle("Bomberman");
        primaryStage.setScene(menuScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((WindowEvent we) -> {
            if (client.isServerOwner()) {
                client.stopServer();
            }
        });

    }

    /**
     * Dle stisknuté klávesy prověd akci (pohyb hráče, umístění bomby...)
     *
     * @param e
     */
    private void dispatchKeyEvents(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT:
                client.movePlayer(Direction.LEFT);
                break;
            case RIGHT:
                client.movePlayer(Direction.RIGHT);
                break;
            case DOWN:
                client.movePlayer(Direction.DOWN);
                break;
            case UP:
                client.movePlayer(Direction.UP);
                break;
            case SPACE:
                client.placeBomb();
                break;
            case ESCAPE:
                leaveGame(); // odejdu ze hry do hlavního menu
                primaryStage.setScene(menuScene);
                break;
            default:
        }
    }

    private void leaveGame() {
        // Pokud se odpojím ze hry a jsem vlastník serveru tak ukončím server, odpojím i ostatní hráče
        if (client.isServerOwner()) {
            client.stopServer();
        } else {
            client.disconnect();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
