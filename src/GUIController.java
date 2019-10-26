import jade.tools.sniffer.AgentList;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.shape.*;
import java.net.URL;
import javafx.scene.layout.*;
import javafx.scene.Group;
import javafx.util.Duration;
import javafx.scene.Scene;
import java.util.List;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

import java.util.ResourceBundle;

public class GUIController implements Initializable {
    //List View variables.
    public MainController MainClass;
    @FXML
    public Text AgentNum;
    @FXML
    public ListView AgentsList;

    @FXML
    public Button createAgentButton;

    @FXML
    private AnchorPane apMain;

    //Draw map on this pane
    @FXML
    private Pane mapPane;

    @FXML
    public ObservableList<Object> DoList = FXCollections.observableArrayList();

    public Scene scene;


    public void initialize(URL url, ResourceBundle rb) {
        createAgentButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
               AddNewAgentWindow();
            }
        });

        AgentsList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                DeleteDeliveryAgentWindow(AgentsList.getSelectionModel().getSelectedIndex());
            }
        });
    }

    @FXML
    public void createAgent(Event e)
    {
        //create agent here

        RefreshGUI();
    }

    public void AddNewAgentWindow()
    {
        Button closeButton = new Button("Close");
        Button saveButton = new Button("Save");
        javafx.scene.control.Label textLabel = new javafx.scene.control.Label("Enter delivery agent capacity: ");
        TextField textField = new TextField();
        Pane secondPane = new Pane();
        secondPane.getChildren().add(textField);
        secondPane.getChildren().add(closeButton);
        secondPane.getChildren().add(saveButton);
        secondPane.getChildren().add(textLabel);
        textLabel.setLayoutX(10);
        textLabel.setLayoutY(10);
        textField.setLayoutX(170);
        textField.setLayoutY(10);
        closeButton.setLayoutX(10);
        closeButton.setLayoutY(50);
        saveButton.setLayoutX(280);
        saveButton.setLayoutY(50);
        Scene newScene = new Scene(secondPane, 350, 100);
        Stage newWindow = new Stage();
        newWindow.setTitle("Add new agent");
        newWindow.setScene(newScene);
        newWindow.show();

        closeButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                newWindow.close();
            }
        });

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try
                {
                    if((textField.getText()!=null && !textField.getText().isEmpty()))
                    {
                        MainClass.addNewDeliveryAgent(Integer.parseInt(textField.getText()));
                        PopulateAgentList();
                        newWindow.close();
                    }
                }
                catch(NumberFormatException e)
                {
                    textLabel.setText("Not a valid number");
                }
            }
        });
    }

    public void DeleteDeliveryAgentWindow(int index)
    {
        Button yesButton = new Button("Yes");
        Button noButton = new Button("No");
        javafx.scene.control.Label textLabel = new javafx.scene.control.Label("Remove selected delivery agent?");
        Pane secondPane = new Pane();
        secondPane.getChildren().add(yesButton);
        secondPane.getChildren().add(noButton);
        secondPane.getChildren().add(textLabel);
        yesButton.setLayoutX(150);
        yesButton.setLayoutY(50);
        noButton.setLayoutX(10);
        noButton.setLayoutY(50);
        Scene newScene = new Scene(secondPane, 200, 100);
        Stage newWindow = new Stage();
        newWindow.setTitle("Remove agent");
        newWindow.setScene(newScene);
        newWindow.show();

        noButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                newWindow.close();
            }
        });

        yesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                MainClass.removeDeliveryAgent(index);
                DoList.remove(index);
                PopulateAgentList();
                newWindow.close();
            }
        });
    }

    @FXML
    public void PopulateAgentList()
    {
        AgentsList.setItems(FXCollections.observableList(DoList));
    }

    @FXML
    public void runButton() {
        MainClass.runAction();
    }

    public void RefreshGUI()
    {
        AgentNum.setText(String.valueOf(DoList.size()-1));
        AgentsList.setItems(FXCollections.observableList(DoList));
    }

    public void RegisterCircle(Circle newCircle) {
        mapPane.getChildren().add(newCircle);
    }

    public void Quit()
    {
        System.exit(0);
    }

    /*
    private void moveCircle(Circle... circles)
    {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                for (Circle circle : circles) {
                    List<TranslateTransition> transitions = new ArrayList<>();
                    for (int i = 0; i < _mapNodes.size(); i++) {
                        TranslateTransition transition = new TranslateTransition(Duration.seconds(1), circle);
                        Circle c = _mapNodes.get(i);
                        Circle cNext = _mapNodes.size() == i + 1 ? _mapNodes.get(0) : _mapNodes.get(i + 1);

                        transition.setByY(cNext.getCenterY() - c.getCenterY());
                        transition.setByX(cNext.getCenterX() - c.getCenterX());

                        transitions.add(transition);
                    }

                    SequentialTransition sequentialTransition = new SequentialTransition(transitions.toArray(new TranslateTransition[transitions.size()]));
                    sequentialTransition.play();
                }
            }
        });
    }

     */
}
