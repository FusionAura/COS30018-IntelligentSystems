import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.*;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import javafx.stage.WindowEvent;

public class MainController extends Application {
    @FXML
    public ListView _agentList;

    private List<Node> _nodes = new ArrayList<>();
    private List<Parcel> _parcels = new ArrayList<>();
    private GUIController _guiController;
    private AgentController _mainAgentController;
    private ContainerController _mainCtrl;


    private Color[] _deliveryColors = {
            Color.BLUE,
            Color.GREEN,
            Color.YELLOW,
            Color.RED,
            Color.AQUA,
            Color.LIME,
            Color.GOLD,
            Color.CRIMSON
    };
    private int _deliveryColorPosition = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Load GUI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("IntelligentSystems.fxml"));
        Parent root = loader.load(); // must be called before getting the controller!
        _guiController = loader.getController();
        primaryStage.setTitle("Intelligent Systems Agent Program");
        primaryStage.setScene(new Scene(root));

        primaryStage.show();

        //Close Button
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                System.out.println("Stage is closing");
                System.exit(0);
            }
        });

        //JADE Startup
        Runtime rt = Runtime.instance();
        System.out.println(MainController.class.getName() + ": Launching the platform Main Container...");
        Profile pMain = new ProfileImpl(null, 8888, null);

        //pMain.setParameter(Profile.GUI, "true");
        _mainCtrl = rt.createMainContainer(pMain);


        // Create and start an agent of class Counter Agent
        System.out.println(MainController.class.getName() + ": Starting up the Master Controller...");
        
        _mainAgentController = _mainCtrl.createNewAgent("MasterRoutingAgent", MasterRoutingAgent.class.getName(), new Object[0]);
        _mainAgentController.start();
        //Register Master Routing position
        _guiController.registerCircle(new Circle(100, 100, 10, Color.CHOCOLATE));
        
        readFromConfigFile();

        //Populate GUI ListView
        _guiController.PopulateAgentList();
        
        _guiController.MainClass = this;
    }

    public void runAction() {
        try {
            List<String> deliveryAgents = new ArrayList<String>();

            for(Object o : _guiController.DoList)
            {
                deliveryAgents.add(o.toString().substring(0, o.toString().indexOf("@")));
            }
            MasterRoutingAgentInterface masterRoutingAgentInterface = _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class);
            masterRoutingAgentInterface.startRouting(deliveryAgents);

            //After we run the algorithm, remove all the parcels (they're being delivered!)
            for (int i = 0; i < _parcels.size();) {
                removeParcel(_parcels.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main (String[] args) throws StaleProxyException
    {
        launch(args);
    }

    public void addNewDeliveryAgent(int capacity) {
        try {
            Circle agentBody = new Circle(100, 100, 5, _deliveryColors[_deliveryColorPosition]);
            _deliveryColorPosition++;
            if (_deliveryColorPosition == _deliveryColors.length) {
                _deliveryColorPosition = 0;
            }

            String agentBodyReference = "d"+(_guiController.DoList.size()+1);
            _guiController.registerCircle(agentBody, agentBodyReference);

            AgentController newDeliveryAgent= _mainCtrl.createNewAgent("d" + (_guiController.DoList.size()+1), DeliveryAgent.class.getName(), new Object[] {agentBody, capacity});
            newDeliveryAgent.start();

            _guiController.DoList.add(newDeliveryAgent.getName());
          
            } catch (StaleProxyException e) {
              e.printStackTrace();
        }
    }

    public void addNode(String name, Position nodePosition) {
        Node node = new Node(name, nodePosition);
        _guiController.registerCircle(node.getBody(), name);
        _nodes.add(node);

        try {
            _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class).newNode(node);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void removeNode(String name) {
        for (Node n : _nodes) {
            if (n.amI(name)) {
                _nodes.remove(n);
                try {
                    _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class).removeNode(n);
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    public void addParcel(Parcel parcel) {
        _parcels.add(parcel);
        _guiController.registerParcel(parcel);

        try {
            _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class).addParcel(parcel);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public void removeDeliveryAgent(int index)  {
        try
        {
            String agentName = _guiController.DoList.get(index).toString();
            _guiController.deregisterCircle(agentName.substring(0, agentName.indexOf("@")));
            _guiController.RefreshGUI();
            _mainCtrl.getAgent(agentName.substring(0, agentName.indexOf("@"))).kill();
        }
        catch (ControllerException e) {
          e.printStackTrace();
        }
    }

    public void removeParcel(Parcel parcel) {
        _parcels.remove(parcel);
        _guiController.unregisterParcel(parcel);

        try {
            _mainAgentController.getO2AInterface(MasterRoutingAgentInterface.class).removeParcel(parcel);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }

    public boolean doesNodeExist(String name) {
        return _nodes.stream().anyMatch(node -> node.amI(name));
    }

    private void readFromConfigFile() {
        CSVFileReader reader = new CSVFileReader();
        List<List<String>> output = reader.readFromFile("app.config");

        for (List<String> line : output) {
            String type = line.get(0);

            switch (type) {
                case "S":
                    // General settings such as number of delivery agents and delivery agent capacity/weight.
                    // S,numOfDeliveryAgents,agent1capacity,agent2capacity...
                    int numOfDeliveryAgents = Integer.parseInt(line.get(1));
                    for (int i = 1; i <= numOfDeliveryAgents; i++) {
                        addNewDeliveryAgent(Integer.parseInt(line.get(i+1)));
                    }
                    break;

                case "N":
                    // Here the node is created. Master routing agent must be notified.
                    // N,posX,posY,nodeName
                    Position nodePosition = new Position(Double.parseDouble(line.get(1)), Double.parseDouble(line.get(2)));

                    addNode(line.get(3), nodePosition);

                    break;

                case "P":
                    // Add in another parcel. Must have a destination.
                    // P,weight,destination,description
                    Parcel parcel = new Parcel(Integer.parseInt(line.get(1)), line.get(2), line.get(3));
                    addParcel(parcel);
                    break;
                default:
                    System.out.println("Invalid line, ignoring");
            }
        }
    }
}
