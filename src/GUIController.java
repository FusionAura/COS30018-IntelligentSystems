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

import java.util.ResourceBundle;

public class GUIController implements Initializable {
    //List View variables.
    public MainController MainClass;
    @FXML
    public Text AgentNum;
    @FXML
    public ListView AgentsList;

    @FXML
    private AnchorPane apMain;

    //Draw map on this pane
    @FXML
    private Pane mapPane;

    @FXML
    public ObservableList<Object> DoList = FXCollections.observableArrayList();

    public Scene scene;


    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    public void createAgent(Event e)
    {
        //create agent here

        RefreshGUI();
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
