import java.net.URL;
import java.util.ResourceBundle;
import java.util.*;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.Scene;


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
    public Button deleteAgentButton;

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

        deleteAgentButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                DeleteDeliveryAgentWindow();
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
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New delivery agent");
        dialog.setHeaderText("Creating new delivery agent");
        dialog.setContentText("Please enter the capacity of the delivery agent");

        int capacity = 0;
        boolean capacityGotten = false;

        do {
            Optional<String> response = dialog.showAndWait();
            if (response.isPresent()) {
                String potentialCapacity = response.get();
                if(potentialCapacity.isBlank())
                {
                    showMessageWindow(Alert.AlertType.ERROR, "ERROR", "Please enter a capacity");
                }
                else {
                    try {
                        capacity = Integer.parseInt(potentialCapacity);
                        capacityGotten = true;
                    } catch (NumberFormatException e) {
                        showMessageWindow(Alert.AlertType.ERROR, "ERROR", "Please enter a value for the capacity");
                    }
                }
            }
            else
            {
                return;
            }
        }while(!capacityGotten);

        MainClass.addNewDeliveryAgent(capacity);
        PopulateAgentList();

        showMessageWindow(Alert.AlertType.INFORMATION, "Success", "New delivery agent added");
    }

    public void DeleteDeliveryAgentWindow()
    {
        int index = AgentsList.getSelectionModel().getSelectedIndex();

        if (index<0||index>=DoList.size()) {
            showMessageWindow(Alert.AlertType.WARNING, "WARNING", "Please select a delivery agent first");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Deleting Delivery Agent");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the selected delivery agent?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MainClass.removeDeliveryAgent(index);
            DoList.remove(index);
            PopulateAgentList();
            showMessageWindow(Alert.AlertType.INFORMATION, "Success", "Delivery agent deleted.");
        }
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

    private void showMessageWindow(Alert.AlertType type, String title, String content) {
        Alert warn = new Alert(type);
        warn.setTitle(title);
        warn.setHeaderText(null);
        warn.setContentText(content);
        warn.showAndWait();
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
