import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.shape.*;
import java.net.URL;
import javafx.scene.layout.*;
import javafx.scene.Group;

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

    public void initialize(URL url, ResourceBundle rb)
    {
        Line line = new Line(10, 10, 100, 100);
        Circle mraShape = new Circle(100, 100, 10, Color.GRAY);
        Circle nodePoint = new Circle(10, 10, 3, Color.BLACK);
        mraShape.setStroke(Color.BLACK);
        mapPane.getChildren().add(line);
        mapPane.getChildren().add(mraShape);
        mapPane.getChildren().add(nodePoint);
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

    public void RefreshGUI()
    {
        AgentNum.setText(String.valueOf(DoList.size()-1));
        AgentsList.setItems(FXCollections.observableList(DoList));
    }

    public void DrawMap()
    {
        Line line = new Line();
        line.setStartX(0);
        line.setEndX(20);
        line.setStartY(0);
        line.setEndY(0);
    }

    public void Quit()
    {
        System.exit(0);
    }
}
