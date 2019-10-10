import javafx.animation.TranslateTransition;
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

    private List<Circle> _mapNodes;

    public void initialize(URL url, ResourceBundle rb)
    {
        List<Circle> nodePoints = new ArrayList<Circle>();
        List<Line> mapLines = new ArrayList<Line>();

        nodePoints.add(new Circle(10, 10, 3, Color.BLACK));
        nodePoints.add(new Circle(190, 10, 3, Color.BLACK));
        nodePoints.add(new Circle(10, 190, 3, Color.BLACK));
        nodePoints.add(new Circle(190, 190, 3, Color.BLACK));
        nodePoints.add(new Circle(100, 100, 3, Color.BLACK));

        _mapNodes = nodePoints;

        mapLines.add(new Line(10, 10, 100, 100));
        mapLines.add(new Line(190, 10, 100, 100));
        mapLines.add(new Line(10, 190, 100, 100));
        mapLines.add(new Line(190, 190, 100, 100));
        mapLines.add(new Line(10, 10, 10, 190));
        mapLines.add(new Line(190, 10, 190, 190));

        for(Line l : mapLines)
        {
            mapPane.getChildren().add(l);
        }

        for(Circle c : nodePoints)
        {
            mapPane.getChildren().add(c);
        }
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
        Circle mraShape = new Circle(100, 100, 10, Color.GRAY);
        mraShape.setStroke(Color.BLACK);
        TranslateTransition transition = createTranslateTransition(mraShape);
        transition.setDuration(Duration.millis(1000));
        transition.setNode(mraShape);
        transition.setCycleCount(1);
        mapPane.getChildren().add(mraShape);



        moveCircle(mraShape, transition);
    }

    public void Quit()
    {
        System.exit(0);
    }

    private TranslateTransition createTranslateTransition(Circle circle)
    {
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.25), circle);
        return transition;
    }

    private void moveCircle(Circle circle, TranslateTransition transition)
    {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                for (Circle c : _mapNodes) {
                    if(circle.getCenterY()>c.getCenterY())
                    {
                        transition.setByY(c.getCenterY() - circle.getCenterY());
                    }
                    else if(circle.getCenterY()<c.getCenterY())
                    {
                        transition.setByY(c.getCenterY() + circle.getCenterY());
                    }
                    else
                    {
                        transition.setByY(0);
                    }

                    if(circle.getCenterX()>c.getCenterX())
                    {
                        transition.setByX(c.getCenterX() - circle.getCenterX());
                    }
                    else if(circle.getCenterX()<c.getCenterX())
                    {
                        transition.setByX(c.getCenterX() + circle.getCenterX());
                    }
                    else
                    {
                        transition.setByX(0);
                    }

                    transition.play();
                    circle.setCenterY(c.getCenterY());
                    circle.setCenterX(c.getCenterX());
                    System.out.println(circle.getCenterX()+", "+circle.getCenterY());
                    System.out.println("Iterated");
                }
            }
        });
    }
}
