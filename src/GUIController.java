import java.net.URL;
import java.util.ResourceBundle;
import java.util.*;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;

public class GUIController implements Initializable {
    //List View variables.
    public MainController MainClass;
    @FXML
    public Text AgentNum;
    @FXML
    public ListView AgentsList;
    @FXML
    public ListView ParcelList;

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
    @FXML
    private ObservableList<Parcel> _parcelViewList = FXCollections.observableArrayList();

    public Scene scene;

    private Map<Circle, Text> _circleReference = new HashMap<>();
    private Circle _highlightedNode = null;
    private Paint _highlightedNodeColor = null;

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

        mapPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                EventTarget target = mouseEvent.getTarget();
                if (target == mapPane) {
                    AddNewNodeWindow(mouseEvent);
                } else {
                    Circle circleTarget = (Circle) target;
                    if (_circleReference.containsKey(circleTarget)) {
                        HighlightNode(circleTarget);
                    }
                }
            }
        });

        ParcelList.setItems(_parcelViewList);
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

    @FXML
    public void createParcelWindow() {
        if (_highlightedNode == null) {
            showMessageWindow(Alert.AlertType.WARNING, "WARNING", "Please select a destination node first");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Parcel");
        dialog.setHeaderText("Creating New Parcel");
        dialog.setContentText("Please enter the weight of the parcel:");
        int weight = 0;
        boolean weightGotten = false;

        do {
            Optional<String> response = dialog.showAndWait();
            if (response.isPresent()) {
                String potentialWeight = response.get();
                if (potentialWeight.isBlank()) {
                    showMessageWindow(Alert.AlertType.ERROR, "ERROR", "Please enter a weight.");
                } else {
                    try {
                        weight = Integer.parseInt(potentialWeight);
                        weightGotten = true;
                    } catch (NumberFormatException e) {
                        //Thrown when potentialWeight is not a number
                        showMessageWindow(Alert.AlertType.ERROR, "ERROR", "Please enter a number for the weight.");
                    }
                }
            } else {
                return;
            }
        } while (!weightGotten);

        //Have to redo it otherwise text input will still have weight in it
        dialog = new TextInputDialog();
        dialog.setTitle("New Parcel");
        dialog.setHeaderText("Creating New Parcel");
        dialog.setContentText("Please enter a description of the parcel:");

        String description = "";
        boolean descriptionGotten = false;

        do {
            Optional<String> response = dialog.showAndWait();
            if (response.isPresent()) {
                String potentialDescription = response.get();
                if (potentialDescription.isBlank()) {
                    showMessageWindow(Alert.AlertType.ERROR, "ERROR", "Please enter a description.");
                } else {
                    description = potentialDescription;
                    descriptionGotten = true;
                }
            } else {
                return;
            }
        } while (!descriptionGotten);

        Parcel parcel = new Parcel(weight, _circleReference.get(_highlightedNode).getText(), description);
        MainClass.addParcel(parcel);

        showMessageWindow(Alert.AlertType.INFORMATION, "Success", "New Parcel Added!");
    }

    @FXML
    public void removeParcelWindow() {
        Parcel selectedParcel = (Parcel) ParcelList.getSelectionModel().getSelectedItem();
        if (selectedParcel == null) {
            showMessageWindow(Alert.AlertType.WARNING, "WARNING", "Please select a parcel first");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Deleting Parcel");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the selected parcel?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MainClass.removeParcel(selectedParcel);

            showMessageWindow(Alert.AlertType.INFORMATION, "Success", "Parcel deleted.");
        }
    }

    public void RefreshGUI()
    {
        AgentNum.setText(String.valueOf(DoList.size()-1));
        AgentsList.setItems(FXCollections.observableList(DoList));
    }

    public void registerCircle(Circle newCircle) {
        mapPane.getChildren().add(newCircle);
    }

    // Use this method if you want to refer to that circle later
    public void registerCircle(Circle newCircle, String reference) {
        newCircle.setPickOnBounds(false);

        Text text = new Text(reference);
        text.setMouseTransparent(true);
        text.setX(newCircle.getCenterX() - text.getBoundsInLocal().getWidth()/2);
        text.setY(newCircle.getCenterY() - text.getBoundsInLocal().getHeight()/2);

        if (!reference.matches("^d\\d$")) {
            mapPane.getChildren().addAll(newCircle, text);
        }
        else
        {
            mapPane.getChildren().addAll(newCircle);
        }

        _circleReference.put(newCircle, text);
    }

    public void deregisterCircle(String reference)
    {
        for(Circle c : _circleReference.keySet())
        {
            if(_circleReference.get(c).getText().equals(reference))
            {
                mapPane.getChildren().remove(c);
            }
        }
    }

    public void registerParcel(Parcel parcel) {
        _parcelViewList.add(parcel);
    }

    public void unregisterParcel(Parcel parcel) {
        _parcelViewList.remove(parcel);
    }

    public void AddNewNodeWindow(MouseEvent mouseEvent) {
        double centerX = mouseEvent.getX();
        double centerY = mouseEvent.getY();
        int size = 5;

        Line tempLine1 = new Line(centerX + size, centerY + size, centerX - size, centerY - size);
        Line tempLine2 = new Line(centerX - size, centerY + size, centerX + size, centerY - size);
        mapPane.getChildren().add(tempLine1);
        mapPane.getChildren().add(tempLine2);

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Node");
        dialog.setHeaderText("Creating New Node");
        dialog.setContentText("Please enter a name for the node:");

        boolean isDone = false;
        do {
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                isDone = true;
                break;
            }

            String newName = result.get();
            if (newName.isBlank()) {
                showMessageWindow(Alert.AlertType.ERROR, "ERROR", "Please enter a name for the new node.");
            } else if (MainClass.doesNodeExist(newName)) {
                showMessageWindow(Alert.AlertType.ERROR, "ERROR", "That name has already been taken. Please enter a different one.");
            } else {
                MainClass.addNode(newName, new Position(centerX, centerY));
                showMessageWindow(Alert.AlertType.INFORMATION, "Success", "New node created");

                isDone = true;
            }
        } while (!isDone);

        mapPane.getChildren().remove(tempLine1);
        mapPane.getChildren().remove(tempLine2);

    }

    public void RemoveNodeWindow() {
        if (_highlightedNode == null) {
            showMessageWindow(Alert.AlertType.WARNING, "WARNING", "Please select a node first.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Deleting Node");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Are you sure you want to delete the highlighted node? This will remove all parcels designed to go to that node!");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            //First, remove all parcels associated with that node
            for (int i = 0; i < _parcelViewList.size();) {
                Parcel parcel = _parcelViewList.get(i);
                if (parcel.getDestination().equals(_circleReference.get(_highlightedNode))) {
                    MainClass.removeParcel(parcel);
                    _parcelViewList.remove(parcel);
                } else {
                    //Increment the counter if we did not delete a parcel
                    //If we did delete a parcel then i will point to the next parcel
                    i++;
                }
            }

            Text textToRemove = _circleReference.get(_highlightedNode);

            MainClass.removeNode(textToRemove.getText());
            _circleReference.remove(_highlightedNode);

            mapPane.getChildren().removeAll(_highlightedNode, textToRemove);

            _highlightedNode = null;
            _highlightedNodeColor = null;

            showMessageWindow(Alert.AlertType.INFORMATION, "Success", "Node deleted.");
        }
    }

    public void HighlightNode(Circle circle) {
        if (_highlightedNode != null) {
            _highlightedNode.setFill(_highlightedNodeColor);
            _circleReference.get(_highlightedNode).setFill(Color.BLACK);
        }
        _highlightedNode = circle;
        _highlightedNodeColor = circle.getFill();

        _highlightedNode.setFill(Color.RED);
        _circleReference.get(_highlightedNode).setFill(Color.RED);
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
}
