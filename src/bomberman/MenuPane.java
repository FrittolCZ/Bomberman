/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;


import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author fanda
 */
public class MenuPane extends BorderPane {

    private final Button createBtn, connectBtn;
    private final GridPane menuPane;
    private final Text lbCaption;

    public MenuPane(Stage primaryStage) {

        createBtn = new Button("Založit hru");
        connectBtn = new Button("Připojit se");

        createBtn.setOnAction((ActionEvent e) -> {
            primaryStage.setScene(Bomberman.createGameScene);
        });

        connectBtn.setOnAction((ActionEvent e) -> {
            primaryStage.setScene(Bomberman.connectGameScene);
        });

        menuPane = new GridPane();

        menuPane.setAlignment(Pos.CENTER);

        menuPane.setHgap(20);
        menuPane.setVgap(10);

        lbCaption = new Text("Bomberman");
        lbCaption.setFont(Font.font(35));

        menuPane.addRow(1, lbCaption);
        menuPane.addRow(2, createBtn);
        menuPane.addRow(3, connectBtn);

        GridPane.setHalignment(lbCaption, HPos.CENTER);
        GridPane.setHalignment(createBtn, HPos.CENTER);
        GridPane.setHalignment(connectBtn, HPos.CENTER);

        this.setCenter(menuPane);
    }

}
