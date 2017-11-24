/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author fanda
 */
public class Client {

    /**
     * Jména hráče tohoto clienta
     */
    private String username;

    /**
     * Port
     */
    private final int port = 4242;

    /**
     * Hrací plocha
     */
    private final MyGameState game;

    /**
     * Socket na kterém naslouchá/posílá zprávy
     */
    private Socket socket;

    /**
     * Vstupní a výstupní stream klienta
     */
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    private final ObservableList<String> playersList = FXCollections.observableArrayList();

    private final IntegerProperty playersCount = new SimpleIntegerProperty(0);

    private final StringBuilder chatText = new StringBuilder();

    private final StringProperty chatProperty = new SimpleStringProperty();

    /**
     * Vlákno na kterém client neustále přijímá zprávy ze serveru
     */
    private ListenFromServer listeningThread;

    /**
     * True pokud tento hráč vytvořil server
     */
    private boolean serverOwner = false;

    /**
     * Hlavní stage
     */
    private final Stage primaryStage;

    private final Timeline timeline;

    private final GamePane pane;

    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private boolean gameRunning = false;

    /**
     *
     * @param primaryStage
     * @param game
     * @param pane
     * @param gameover
     */
    public Client(Stage primaryStage, MyGameState game, GamePane pane, Text gameover) {
        this.primaryStage = primaryStage;
        this.game = game;
        this.pane = pane;
        timeline = new Timeline();
        KeyFrame updates = new KeyFrame(Duration.millis(Bomberman.DELAY), e -> {
            if (!game.update(Bomberman.DELAY)) {
                gameover.setText("Vítězem se stává hráč: " + game.lastPlayerName());
                timeline.stop();
                gameRunning = false;
                primaryStage.setScene(Bomberman.gameOverScene);
            }
        }
        );
        timeline.getKeyFrames().add(updates);
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Uloží jméno hráče tohoto klienta
     *
     * @param name
     */
    public StringProperty getChatProperty() {
        return chatProperty;
    }

    public ObservableList<String> getPlayers() {
        return this.playersList;
    }

    private Player getThisPlayer() {
        return game.getPlayerByName(username);
    }

    public final BooleanProperty connectedProperty() {
        return this.connected;
    }

    public final IntegerProperty getPlayerCountProperty() {
        return playersCount;
    }

    /**
     *
     * @return True pokud tento klient založil server
     */
    public boolean isServerOwner() {
        return serverOwner;
    }

    /**
     * Nastaví tohoto klienta jako vlastníka serveru
     */
    public void setServerOwner() {
        serverOwner = true;
    }

    /**
     * Připojí tohoto klienta na server
     *
     * @return True pokud připojení proběhlo úspěšně
     */
    public boolean start(String username) {
        // Pokusí se připojit na server
        this.username = username;
        try {
            socket = new Socket("localhost", port);
        } catch (IOException ec) {
            System.out.println("Chyba připojení k serveru:" + ec);
            return false;
        }
        // inicializuje streamy
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());

        } catch (IOException eIO) {
            System.out.println("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // vytvoří a spustí vlákno na kterém naslouhcá ze serveru
        listeningThread = new ListenFromServer();
        listeningThread.start();

        try {
            // Pošle serveru jméno tohoto klienta
            sOutput.writeObject(username);
        } catch (IOException eIO) {
            return false;
        }
        connected.set(true);
        return true;
    }

    /**
     * Pošle na server zprávu
     *
     * @param msg - text zprávy
     */
    private void sendMessage(String msg) {
        try {
            sOutput.writeObject(msg);
        } catch (IOException e) {
            System.out.println("Nastala chyba při zasílání zprývy serveru: " + e);
        }
    }

    /**
     * Pošle textovou zprávu na server podepsanou jménem tohoto klienta
     *
     * @param msgText - text zprávy
     */
    public void sendChatMsg(String msgText) {
        sendMessage("MSG " + username + " " + msgText);
    }

    /**
     * Pošle serveru správu že se má zastavit
     */
    public void stopServer() {
        serverOwner = false;
        sendMessage("STOP " + username);
    }

    /**
     * Rozdělí zprávu a podle prvniho slova (identifikátoru) provede činnost
     *
     * @param msg
     */
    private void msgAct(String msg) {
        final String[] parts = msg.split(" ");
        switch (parts[0]) {
            // zpráva obsahuje jména hráčů připojených na server
            case "PLAYERS_CONNECTED":
                Platform.runLater(() -> {
                    playersList.clear();
                    for (int i = 1; i < parts.length; i++) {
                        playersList.add(parts[i]);
                    }
                    playersCount.set(playersList.size());
                });
                break;
            // Textová zpráva - obsahuje jméno autora a text
            case "MSG":
                chatText.append(parts[1]).append(": ").append(getMsgText(parts));
                chatProperty.set(chatText.toString());
                break;
            // spustí hru (změní scenu)
            case "START":
                game.initGame((ArrayList<String>) playersList.stream().collect(Collectors.toList()));
                gameRunning = true;
                timeline.play();
                Platform.runLater(() -> {
                    primaryStage.setScene(Bomberman.gameScene);
                });
                break;
            case "MOVE":
                game.movePlayer(parts[1], parts[2]);
                break;
            case "PLANT":
                Bomb b = game.plantBomb(parts[1]);
                if (b != null) {
                    Platform.runLater(() -> {
                        pane.drawBomb(b);
                    });
                }
                break;
            case "DISCONNECTED":
                if (!gameRunning) {
                    Platform.runLater(() -> {
                        for (int i = 0; i <= playersList.size(); i++) {
                            if (playersList.get(i).equals(parts[1])) {
                                playersList.set(i, "");
                                playersList.remove(i);
                            }
                        }
                        playersCount.set(playersList.size());
                    });
                } else {
                    game.killPlayer(parts[1]);
                }
                break;
            default:
                break;
        }
    }

    /**
     *
     * @param parts Části přijaté zprávy
     * @return Vrátí string bez identifikátoru zprávy a autora zprávy
     */
    private String getMsgText(String[] parts) {
        StringBuilder text = new StringBuilder();
        for (int i = 2; i < parts.length; i++) {
            text.append(parts[i]).append(" ");
        }
        text.append("\n");
        return text.toString();
    }

    /**
     * Pošle na server správu o spuštění hry
     */
    void startGame() {
        sendMessage("STARTGAME " + username);
    }

    /**
     * Ověří zda pohyb tímto směrem je možný, pokud ano pošle serveru zprávu o
     * pohybu
     *
     * @param direction Požadovaný směr pohybu
     */
    void movePlayer(Direction direction) {
        if (!game.checkMove(username, direction)) {
            sendMessage("MOVE " + username + " " + direction.getName());
        }
    }

    void placeBomb() {
        sendMessage("PLANT " + username);
    }

    void disconnect() {
        sendMessage("DISCONNECT " + username);
    }

    /**
     * Vlákno na kterém client v nekonečné smičce čte zprávy ze serveru
     */
    class ListenFromServer extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.println("CLIENT " + username + " RECIEVED: " + msg);
                    msgAct(msg);
                } catch (IOException e) {
                    System.out.println("Server ukončil spojení: " + e);
                    Platform.runLater(() -> {
                        connected.set(false);
                        gameRunning = false;
                        playersList.clear();
                        playersCount.set(0);
                        primaryStage.setScene(Bomberman.menuScene);
                    });
                    break;
                } catch (ClassNotFoundException ex) {
                    System.out.println("Neznámá třída: " + ex);
                }
            }
        }

    }

}
