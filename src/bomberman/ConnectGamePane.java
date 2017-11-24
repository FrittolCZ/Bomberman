/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author fanda
 */
public class ConnectGamePane extends GridPane {

    private final TextField nameField, msgField;
    private final Text lbCaption;
    private final Button connectBtn, sendBtn;
    private final TextArea chatArea;
    private final ListView connectedPlayers;
    private final Client client;

    public ConnectGamePane(Stage primaryStage, Client client) {
        this.client = client;
        lbCaption = new Text("Založit hru");
        lbCaption.setFont(Font.font(20));

        nameField = new TextField();
        nameField.editableProperty().bind(client.connectedProperty().not());

        connectedPlayers = new ListView();
        connectedPlayers.setItems(client.getPlayers());

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.textProperty().bind(client.getChatProperty());

        msgField = new TextField();
        msgField.disableProperty().bind(client.connectedProperty().not());
        msgField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendChatMessage();
            }
        });

        connectBtn = new Button("Připojit se");
        connectBtn.setOnAction(e -> {
            String username = nameField.getText().trim();
            if (username.length() != 0) {
                client.start(username);
            } else {
                System.out.println("Neplatné jméno!");
            }
        });

        connectBtn.disableProperty().bind(client.connectedProperty());
        sendBtn = new Button("Odeslat");
        sendBtn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        sendBtn.setOnAction(e -> {
            sendChatMessage();
        });

        this.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                if (connectBtn.isDisabled()) {
                    client.disconnect();
                    msgField.clear();
                }
                primaryStage.setScene(Bomberman.menuScene);
            }
        });

        GridPane.setHgrow(msgField, Priority.ALWAYS);

        GridPane msgPane = new GridPane();

        this.setAlignment(Pos.TOP_CENTER);
        this.setHgap(20);
        this.setVgap(10);
        this.setPadding(new Insets(10, 10, 10, 10));

        super.add(lbCaption, 0, 0, 2, 1);
        super.addRow(1, new Text("Jméno hráče"), nameField);
        super.addRow(2, connectBtn);

        super.addRow(3, new Text("Chat:"), new Text("Připojení hráči:"));

        super.addRow(4, chatArea, connectedPlayers);

        msgPane.addRow(0, new Text("Zpráva: "), msgField);

        super.addRow(5, msgPane, sendBtn);
    }

    private void sendChatMessage() {
        String msgText = msgField.getText();
        if (msgText.length() != 0) {
            client.sendChatMsg(msgText);
        }
        msgField.setText("");
    }
}
