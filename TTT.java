package Summer2022Project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * @author: Johnny Zheng
 * 
 * Main class for the Tic Tac Toe program. Sets up the window.
 */

public class TTT extends Application {
  
  private int boxSize = 200;

  @Override // Override the start method in the Application class
  public void start(Stage stage) {
    stage.setTitle("TicTacToe");
    stage.setHeight(760);
    stage.setWidth(630);
    
    
    GridSpace gridLayout = new GridSpace(boxSize);
    BorderPane bPane = new BorderPane(gridLayout);
    Scene scene = new Scene(bPane);
    

    HBox hBox = new HBox();
    hBox.getChildren().add(gridLayout.newGameMenuBtn);
    bPane.setBottom(hBox);
    hBox.setSpacing(10);
    hBox.setPadding(new Insets(10));
    hBox.setAlignment(Pos.BOTTOM_CENTER);
    
    bPane.setBottom(hBox);
    bPane.setTop(gridLayout.header);
    BorderPane.setAlignment(gridLayout.header, Pos.TOP_CENTER);
    BorderPane.setMargin(gridLayout.header, new Insets(20, 0, 0, 0));
    
    gridLayout.header.setText("");
    gridLayout.header.setFont(Font.font(20));
    
    stage.setResizable(false);
    stage.setAlwaysOnTop(true);
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}